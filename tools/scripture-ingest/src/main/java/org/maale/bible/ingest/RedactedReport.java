package org.maale.bible.ingest;
import java.io.*; import java.nio.file.*; import java.util.*;
public final class RedactedReport {
  private RedactedReport() {}
  public static void write(Path destination,String sourceId,String archiveSha,String technicalStatus,int files,int books,List<String> errors) throws IOException {
    for(String e:errors) if(e==null || !e.matches("[A-Z0-9_:-]+")) throw new IOException("REPORT_REDACTION_REJECTED");
    String json="{\n  \"reportVersion\": \"1.0.0\",\n  \"sourceId\": \""+safe(sourceId)+"\",\n  \"archiveSha256\": \""+archiveSha+"\",\n  \"technicalStatus\": \""+technicalStatus+"\",\n  \"rightsStatus\": \"A2_BASELINE_ACCEPTED_WITH_EXCEPTION\",\n  \"provenanceStatus\": \"DOCUMENTARY_CAVEAT_REMAINS\",\n  \"linguisticStatus\": \"DEFERRED\",\n  \"filesChecked\": "+files+",\n  \"booksFound\": "+books+",\n  \"errors\": ["+String.join(",",errors.stream().map(x->\"\\""+x+"\"").toList())+"],\n  \"scriptureTextIncluded\": false\n}\n";
    Files.createDirectories(destination.getParent()); Files.writeString(destination,json,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
    String upper=Files.readString(destination).toUpperCase(Locale.ROOT); if(upper.contains("VERSE_TEXT") || upper.contains("SCRIPTURE_TEXT_INCLUDED\": TRUE")) {Files.deleteIfExists(destination);throw new IOException("REPORT_REDACTION_REJECTED");}
  }
  private static String safe(String s){if(s==null || !s.matches("[A-Za-z0-9._-]+"))throw new IllegalArgumentException("INVALID_SOURCE_ID");return s;}
}