package org.maale.bible.ingest;
import java.io.*; import java.nio.file.*; import java.util.*;
public final class ValidationPipeline {
  public record Result(String status,int filesChecked,int booksFound,List<String> errorCodes) {}
  private ValidationPipeline() {}
  public static Result run(Path archive,String expectedArchiveSha,Path staging,List<String> expectedBookIds,Map<String,String> expectedFileHashes,ArchiveSafetyValidator.Limits limits) {
    try {
      ArchiveSafetyValidator.extractVerified(archive,expectedArchiveSha,staging,limits);
      int checked=0;
      for(var entry:expectedFileHashes.entrySet()){
        Path file=staging.resolve(entry.getKey()).normalize();
        if(!file.startsWith(staging.toAbsolutePath().normalize()) && !file.startsWith(staging.normalize())) return new Result("FAIL_FILE_INTEGRITY",checked,0,List.of("PATH_ESCAPE"));
        if(!Files.isRegularFile(file)) return new Result("FAIL_FILE_INTEGRITY",checked,0,List.of("EXPECTED_FILE_MISSING"));
        Sha256Verifier.verify(file,entry.getValue()); UnicodeValidator.validate(file); checked++;
      }
      Set<String> ids=UsfmInventoryValidator.validate(staging,expectedBookIds);
      return new Result("PASS_TECHNICAL_VALIDATION",checked,ids.size(),List.of());
    } catch(Exception e) {
      String code=e.getMessage()==null?"VALIDATION_FAILED":e.getMessage().replaceAll("[^A-Z0-9_:-]","_").toUpperCase(Locale.ROOT);
      return new Result("FAIL_CLOSED",0,0,List.of(code));
    }
  }
}