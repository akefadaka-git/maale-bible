package org.maale.bible.ingest;
import static org.junit.jupiter.api.Assertions.*; import java.nio.file.*; import org.junit.jupiter.api.Test;
class Sha256VerifierTest {
 @Test void verifiesKnownDigest() throws Exception {Path p=Files.createTempFile("sha","txt");Files.writeString(p,"abc");assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",Sha256Verifier.sha256(p));}
 @Test void rejectsMismatch() throws Exception {Path p=Files.createTempFile("sha","txt");Files.writeString(p,"changed");assertThrows(java.io.IOException.class,()->Sha256Verifier.verify(p,"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"));}
}