package org.maale.bible.ingest;
import java.io.*; import java.nio.file.*; import java.util.*;
public final class ValidationPipeline {
  public record Result(String status,int filesChecked,int booksFound,List<String> errorCodes) {}
  private ValidationPipeline() {}
  public static Result run(Path archive,String expectedArchiveSha,Path staging,List<String> expectedBookIds,Map<String,String> expectedFileHashes,ArchiveSafetyValidator.Limits limits) {
    try {
      if(expectedFileHashes.size()!=expectedBookIds.size()) throw new IOException("FILE_HASH_MANIFEST_COUNT_MISMATCH");
      ArchiveSafetyValidator.extractVerified(archive,expectedArchiveSha,staging,limits);
      Path root=staging.toRealPath(); int checked=0;
      for(var entry:expectedFileHashes.entrySet()){
        Path file=root.resolve(entry.getKey()).normalize();
        if(!file.startsWith(root)) throw new IOException("PATH_ESCAPE");
        if(!Files.isRegularFile(file)) throw new IOException("EXPECTED_FILE_MISSING");
        Sha256Verifier.verify(file,entry.getValue()); UnicodeValidator.validate(file); checked++;
      }
      Set<String> ids=UsfmInventoryValidator.validate(root,expectedBookIds);
      return new Result("PASS_TECHNICAL_VALIDATION",checked,ids.size(),List.of());
    } catch(Exception e) {
      try { deleteTree(staging); } catch(IOException ignored) { /* retain stable failure code; restricted operator must inspect cleanup state */ }
      String code=e.getMessage()==null?"VALIDATION_FAILED":e.getMessage().replaceAll("[^A-Z0-9_:-]","_").toUpperCase(Locale.ROOT);
      return new Result("FAIL_CLOSED",0,0,List.of(code));
    }
  }
  private static void deleteTree(Path root) throws IOException {
    if(root==null || !Files.exists(root)) return;
    try(var paths=Files.walk(root)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);}
  }
}