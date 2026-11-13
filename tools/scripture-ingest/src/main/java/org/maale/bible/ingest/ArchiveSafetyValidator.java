package org.maale.bible.ingest;
import java.io.*; import java.nio.file.*; import java.util.*; import org.apache.commons.compress.archivers.zip.*;
public final class ArchiveSafetyValidator {
  public record Limits(int maxEntries,long maxEntryBytes,long maxExpandedBytes,double maxCompressionRatio) {}
  public static void extractVerified(Path archive,String expectedSha,Path staging,Limits limits) throws IOException {
    Sha256Verifier.verify(archive,expectedSha);
    if(Files.exists(staging) && Files.list(staging).findAny().isPresent()) throw new IOException("STAGING_NOT_EMPTY");
    Files.createDirectories(staging); Path root=staging.toRealPath(); Set<String> seen=new HashSet<>(); long total=0; int count=0;
    try(ZipFile zip=new ZipFile(archive.toFile())) {
      Enumeration<ZipArchiveEntry> entries=zip.getEntries();
      while(entries.hasMoreElements()){
        ZipArchiveEntry e=entries.nextElement(); if(++count>limits.maxEntries()) throw new IOException("ENTRY_COUNT_LIMIT");
        String raw=e.getName(); if(raw==null || raw.indexOf('\0')>=0 || raw.startsWith("/") || raw.startsWith("\\\\") || raw.matches("^[A-Za-z]:.*")) throw new IOException("UNSAFE_ARCHIVE_PATH");
        Path rel=Path.of(raw.replace('\\','/')).normalize();
        if(rel.isAbsolute() || rel.getNameCount()==0 || rel.startsWith("..") || raw.contains("../") || raw.contains("..\\") ) throw new IOException("PATH_TRAVERSAL");
        String key=rel.toString().toLowerCase(Locale.ROOT); if(!seen.add(key)) throw new IOException("DUPLICATE_NORMALIZED_PATH");
        if(e.isUnixSymlink() || !zip.canReadEntryData(e)) throw new IOException("UNSUPPORTED_ARCHIVE_ENTRY");
        if(e.isDirectory()){Path dir=root.resolve(rel).normalize(); if(!dir.startsWith(root)) throw new IOException("PATH_ESCAPE"); Files.createDirectories(dir); continue;}
        long declared=e.getSize(); if(declared<0 || declared>limits.maxEntryBytes()) throw new IOException("ENTRY_SIZE_LIMIT");
        total=Math.addExact(total,declared); if(total>limits.maxExpandedBytes()) throw new IOException("EXPANDED_SIZE_LIMIT");
        long compressed=e.getCompressedSize(); if(compressed>0 && ((double)declared/compressed)>limits.maxCompressionRatio()) throw new IOException("COMPRESSION_RATIO_LIMIT");
        Path out=root.resolve(rel).normalize(); if(!out.startsWith(root)) throw new IOException("PATH_ESCAPE"); Files.createDirectories(out.getParent());
        try(InputStream in=zip.getInputStream(e); OutputStream os=Files.newOutputStream(out,StandardOpenOption.CREATE_NEW)){
          byte[] buf=new byte[32768]; long written=0; int n; while((n=in.read(buf))!=-1){written+=n;if(written>limits.maxEntryBytes())throw new IOException("ENTRY_SIZE_LIMIT");os.write(buf,0,n);}
          if(written!=declared) throw new IOException("ENTRY_LENGTH_MISMATCH");
        } catch(IOException ex){Files.deleteIfExists(out); throw ex;}
      }
    } catch(ArithmeticException e){throw new IOException("EXPANDED_SIZE_OVERFLOW",e);}
  }
}