package org.maale.bible.ingest;
import static org.junit.jupiter.api.Assertions.*; import java.nio.file.*; import java.util.*; import org.junit.jupiter.api.Test;
class UsfmInventoryValidatorTest {
 @Test void validatesExactSyntheticInventory() throws Exception {Path d=Files.createTempDirectory("inv");Files.writeString(d.resolve("GEN.usfm"),"\\id GEN Synthetic\n\\c 1\n\\v 1 synthetic fixture\n");assertEquals(Set.of("GEN"),UsfmInventoryValidator.validate(d,List.of("GEN")));}
 @Test void failsClosedOnMissingOrDuplicateBook() throws Exception {Path d=Files.createTempDirectory("inv");Files.writeString(d.resolve("GEN.usfm"),"\\id GEN Synthetic\n");assertThrows(java.io.IOException.class,()->UsfmInventoryValidator.validate(d,List.of("GEN","EXO")));}
}