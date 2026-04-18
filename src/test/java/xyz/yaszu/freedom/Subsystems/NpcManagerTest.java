package xyz.yaszu.freedom.Subsystems;

import org.junit.jupiter.api.Test;
import xyz.yaszu.freedom.Util.CopypartyClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcManagerTest {

    @Test
    void getRandomNpcSkin_usesRemoteClientValueInsteadOfFallback() {
        String remoteSkin = "https://files.yaszu.xyz/skins/remote-random.png";
        CopypartyClient stubClient = new CopypartyClient() {
            @Override
            public String fetchRandomSkinOrFallback(List<String> fallbackSkins) {
                assertFalse(fallbackSkins.isEmpty());
                assertTrue(fallbackSkins.stream().allMatch(url -> url.startsWith("https://s.namemc.com/i/")));
                return remoteSkin;
            }
        };

        String selected = NpcManager.getRandomNpcSkin(stubClient);

        assertEquals(remoteSkin, selected);
    }

    @Test
    void getRandomNpcSkin_storesSelectedSkinInResourcesFile() throws IOException {
        String remoteSkin = "https://files.yaszu.xyz/skins/remote-random.png";
        CopypartyClient stubClient = new CopypartyClient() {
            @Override
            public String fetchRandomSkinOrFallback(List<String> fallbackSkins) {
                return remoteSkin;
            }
        };

        String selected = NpcManager.getRandomNpcSkin(stubClient);
        Path output = Path.of("src", "main", "resources", "namemc-random-skin-test.txt");
        Files.writeString(output, selected + System.lineSeparator(), StandardCharsets.UTF_8);

        String persisted = Files.readString(output, StandardCharsets.UTF_8).trim();
        assertEquals(remoteSkin, persisted);
    }
}

