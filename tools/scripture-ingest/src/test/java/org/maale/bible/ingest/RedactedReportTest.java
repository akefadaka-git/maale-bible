package org.maale.bible.ingest;
import static org.junit.jupiter.api.Assertions.*; import java.nio.file.*; import java.util.*; import org.junit.jupiter.api.Test;
class RedactedReportTest {
 @Test void writesOnlyRedactedMetadata() throws Exception {Path p=Files.createTempDirectory("report").resolve("report.json");RedactedReport.write(p,"MDYTWF","4".repeat(64),"PASS_SYNTHETIC",1,1,List.of());String s=Files.readString(p);assertTrue(s.contains("\"scriptureTextIncluded\": false"));assertFalse(s.contains("synthetic fixture"));assertFalse(s.contains("verse text"));}
 @Test void rejectsFreeformErrorText() throws Exception {Path p=Files.createTempDirectory("report").resolve("report.json");assertThrows(java.io.IOException.class,()->RedactedReport.write(p,"MDYTWF","4".repeat(64),"FAIL",0,0,List.of("contains scripture text")));}
}