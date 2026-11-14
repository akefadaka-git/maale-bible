package org.maale.bible.ingest;
import java.io.*; import java.nio.file.*; import java.security.*; import java.util.HexFormat;
public final class Sha256Verifier {
  private Sha256Verifier() {}
  public static String sha256(Path path) throws IOException {
    try { MessageDigest md=MessageDigest.getInstance("SHA-256"); try(InputStream in=Files.newInputStream(path)){byte[] b=new byte[65536]; int n; while((n=in.read(b))!=-1) md.update(b,0,n);} return HexFormat.of().formatHex(md.digest()); }
    catch(NoSuchAlgorithmException e){throw new IllegalStateException("SHA-256_UNAVAILABLE",e);}
  }
  public static void verify(Path path,String expected) throws IOException {
    if(expected==null || !expected.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("INVALID_EXPECTED_SHA256");
    if(!sha256(path).equals(expected)) throw new IOException("SHA256_MISMATCH");
  }
}