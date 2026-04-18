package xyz.yaszu.freedom.Util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopypartyClientTest {

    private static final String SAMPLE_JSON = """
            [{
              "files": [
                {"name": "skin-a.png"},
                {"name": "skin-b.jpg"},
                {"name": "not-a-skin.txt"}
              ]
            }]
            """;

    private static final String SAMPLE_HTML = """
            <table id=\"files\"><tbody>
              <tr><td><a href=\"remote-1.png\">remote-1.png</a></td></tr>
              <tr><td><a href=\"/skins/remote-2.jpg?dl=1\">remote-2.jpg</a></td></tr>
              <tr><td><a href=\"README.txt\">README.txt</a></td></tr>
            </tbody></table>
            """;

    @Test
    void extractSkinUrls_onlyReturnsSupportedSkinFiles() {
        List<String> urls = CopypartyClient.extractSkinUrls(SAMPLE_JSON, "https://files.yaszu.xyz/skins/");

        assertTrue(urls.contains("https://files.yaszu.xyz/skins/skin-a.png"));
        assertTrue(urls.contains("https://files.yaszu.xyz/skins/skin-b.jpg"));
        assertFalse(urls.stream().anyMatch(url -> url.endsWith(".txt")));
    }

    @Test
    void extractSkinUrls_supportsHtmlListingFallback() {
        List<String> urls = CopypartyClient.extractSkinUrls(SAMPLE_HTML, "https://files.yaszu.xyz/skins/");

        assertTrue(urls.contains("https://files.yaszu.xyz/skins/remote-1.png"));
        assertTrue(urls.contains("https://files.yaszu.xyz/skins/remote-2.jpg"));
        assertFalse(urls.stream().anyMatch(url -> url.endsWith(".txt")));
    }

    @Test
    void selectRandomSkinFromJson_prefersRemoteOverFallback() {
        String fallback = "https://s.namemc.com/i/fallback.png";

        String selected = CopypartyClient.selectRandomSkinFromJson(
                SAMPLE_JSON,
                "https://files.yaszu.xyz/skins/",
                List.of(fallback),
                new Random(0)
        );

        assertTrue(selected.startsWith("https://files.yaszu.xyz/skins/"));
        assertFalse(selected.equals(fallback));
    }

    @Test
    void writesRandomNpcSkinToResourcesFile() throws IOException {
        String selected = CopypartyClient.selectRandomSkinFromJson(
                SAMPLE_JSON,
                "https://files.yaszu.xyz/skins/",
                List.of("https://s.namemc.com/i/fallback.png"),
                new Random()
        );

        Path output = Path.of("src", "main", "resources", "namemc-random-skin-test.txt");
        Files.writeString(output, selected + System.lineSeparator(), StandardCharsets.UTF_8);

        String persisted = Files.readString(output, StandardCharsets.UTF_8).trim();
        assertTrue(persisted.startsWith("https://files.yaszu.xyz/skins/"));
    }
}

