package org.maale.bible.ingest;
import java.io.*; import java.nio.*; import java.nio.charset.*; import java.nio.file.*; import java.text.Normalizer;
public final class UnicodeValidator {
  public static void validate(Path file) throws IOException {
    byte[] bytes=Files.readAllBytes(file); CharsetDecoder d=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
    final String s; try{s=d.decode(ByteBuffer.wrap(bytes)).toString();}catch(CharacterCodingException e){throw new IOException("INVALID_UTF8",e);}
    if(s.startsWith("\uFEFF")) throw new IOException("UTF8_BOM_NOT_ALLOWED");
    if(!Normalizer.isNormalized(s,Normalizer.Form.NFC)) throw new IOException("UNICODE_NOT_NFC");
    for(int i=0;i<s.length();){int cp=s.codePointAt(i); if(cp==0xFFFD)throw new IOException("UNICODE_REPLACEMENT_CHARACTER"); if(Character.isISOControl(cp) && cp!=9 && cp!=10 && cp!=13) throw new IOException("UNEXPECTED_CONTROL_CHARACTER"); i+=Character.charCount(cp);}
  }
}