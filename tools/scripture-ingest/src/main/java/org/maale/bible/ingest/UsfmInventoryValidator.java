package org.maale.bible.ingest;
import java.io.*; import java.nio.file.*; import java.util.*; import java.util.regex.*;
public final class UsfmInventoryValidator {
  private static final Pattern ID=Pattern.compile("(?m)^\\\\id\\s+([A-Z0-9]{3})(?:\\s|$)");
  public static Set<String> validate(Path root,List<String> expected) throws IOException {
    Set<String> found=new TreeSet<>(); int files=0;
    try(var walk=Files.walk(root)){for(Path p:walk.filter(Files::isRegularFile).filter(x->x.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".usfm")).toList()){
      files++; UnicodeValidator.validate(p); String s=Files.readString(p); Matcher m=ID.matcher(s); if(!m.find())throw new IOException("BOOK_ID_MISSING"); String id=m.group(1); if(m.find())throw new IOException("DUPLICATE_BOOK_ID_IN_FILE"); if(!found.add(id))throw new IOException("DUPLICATE_BOOK_ID");
    }}
    if(files!=expected.size() || found.size()!=expected.size() || !found.equals(new TreeSet<>(expected))) throw new IOException("BOOK_INVENTORY_MISMATCH");
    return Collections.unmodifiableSet(found);
  }
}