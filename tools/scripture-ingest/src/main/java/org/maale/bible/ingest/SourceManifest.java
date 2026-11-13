package org.maale.bible.ingest;
import com.fasterxml.jackson.databind.*; import java.io.*; import java.nio.file.*; import java.util.*;
public record SourceManifest(String sourceId,String editionId,String languageTag,String archiveSha256,List<String> expectedBookIds,int expectedFileCount,String licenceId,String distributionState) {
  public static SourceManifest read(Path file) throws IOException {
    JsonNode n=new ObjectMapper().readTree(file.toFile());
    for(String forbidden:List.of("verseText","scriptureText","passages","contentPayload")) if(n.has(forbidden)) throw new IOException("MANIFEST_MUST_BE_METADATA_ONLY");
    String sha=n.path("expectedArchiveSha256").asText();
    if(!sha.matches("[a-f0-9]{64}")) throw new IOException("INVALID_ARCHIVE_SHA256");
    List<String> ids=new ArrayList<>(); n.path("expectedBookIds").forEach(x->ids.add(x.asText()));
    if(ids.size()!=66 || new HashSet<>(ids).size()!=66) throw new IOException("INVALID_66_BOOK_INVENTORY");
    if(!"NOT_APPROVED_BY_THIS_MANIFEST".equals(n.path("distributionState").asText())) throw new IOException("DISTRIBUTION_STATE_MUST_FAIL_CLOSED");
    return new SourceManifest(n.path("sourceId").asText(),n.path("editionId").asText(),n.path("languageTag").asText(),sha,List.copyOf(ids),n.path("expectedFileCount").asInt(),n.path("licenceId").asText(),n.path("distributionState").asText());
  }
}