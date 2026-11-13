package org.maale.bible.ingest;
import static org.junit.jupiter.api.Assertions.*; import java.nio.file.*; import org.junit.jupiter.api.Test;
class SourceManifestTest {
 @Test void loadsFrozenMetadataOnlyManifest() throws Exception {Path p=Path.of("../../content/manifests/maale-p1-source-manifest.v1.json");SourceManifest m=SourceManifest.read(p);assertEquals("MDYTWF",m.sourceId());assertEquals(66,m.expectedBookIds().size());assertEquals("NOT_APPROVED_BY_THIS_MANIFEST",m.distributionState());}
}