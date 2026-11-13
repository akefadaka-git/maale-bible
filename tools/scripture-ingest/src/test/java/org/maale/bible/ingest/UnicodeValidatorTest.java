package org.maale.bible.ingest;
import static org.junit.jupiter.api.Assertions.*; import java.nio.file.*; import org.junit.jupiter.api.Test;
class UnicodeValidatorTest {
 @Test void acceptsNfcAndEthiopic() throws Exception {Path p=Files.createTempFile("uni","txt");Files.writeString(p,"ሰላም\n");assertDoesNotThrow(()->UnicodeValidator.validate(p));}
 @Test void rejectsMalformedUtf8() throws Exception {Path p=Files.createTempFile("uni","txt");Files.write(p,new byte[]{(byte)0xC3,0x28});assertThrows(java.io.IOException.class,()->UnicodeValidator.validate(p));}
 @Test void rejectsNonNfc() throws Exception {Path p=Files.createTempFile("uni","txt");Files.writeString(p,"e\u0301");assertThrows(java.io.IOException.class,()->UnicodeValidator.validate(p));}
 @Test void rejectsReplacementAndControls() throws Exception {Path p=Files.createTempFile("uni","txt");Files.writeString(p,"x\uFFFD");assertThrows(java.io.IOException.class,()->UnicodeValidator.validate(p));}
}