package xyz.yaszu.freedom.Subsystems;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.icu.impl.Pair;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SkinLoader {
    private static final String BASE_URL = "https://files.yaszu.xyz/skins/";
    private static final Random random = new Random();

    public static Pair<List<TextureProperty>,String> loadRandomSkin() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?ls"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();

            String skinFilename = "";

            // SCENARIO A: If copyparty returns a simple list of raw text filenames ["skin1.png", "skin2.png"]
            try {
                Type stringListType = new TypeToken<ArrayList<String>>(){}.getType();
                List<String> stringList = gson.fromJson(response.body(), stringListType);
                if (stringList != null && !stringList.isEmpty()) {
                    skinFilename = stringList.get(random.nextInt(stringList.size()));
                }
            } catch (Exception e) {
                // SCENARIO B: If copyparty returns an array of metadata objects [{"p":"skin1.png", "s":1024}]
                Type mapListType = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> mapList = gson.fromJson(response.body(), mapListType);
                if (mapList != null && !mapList.isEmpty()) {
                    Map<String, Object> randomFile = mapList.get(random.nextInt(mapList.size()));
                    // "p" is the typical key copyparty uses for the relative file path/name
                    skinFilename = randomFile.get("p").toString();
                }
            }

            if (skinFilename.isEmpty()) {
                System.out.println("No skins found on the server.");
                return null;
            }

            List<TextureProperty> textures = actuallyConstruct(skinFilename);
            return Pair.of(textures,skinFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @NonNull List<TextureProperty> actuallyConstruct(String skinFilename) {
        // 1. Construct the direct, absolute download URL for the chosen skin
        String skinUrl = BASE_URL + skinFilename;

        // 2. Build the standard Minecraft texture JSON payload
        String textureJson = "{\n" +
                "  \"textures\": {\n" +
                "    \"SKIN\": {\n" +
                "      \"url\": \"" + skinUrl + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // 3. Encode the JSON payload into Base64 (required by Mojang/Minecraft systems)
        String base64Value = Base64.getEncoder().encodeToString(textureJson.getBytes(StandardCharsets.UTF_8));

        // 4. Construct your TextureProperty list
        List<TextureProperty> textures = new ArrayList<>();
        // Assuming TextureProperty follows the (Name, Value) or (Name, Value, Signature) constructor
        textures.add(new TextureProperty("textures", base64Value,null));
        System.out.println("Successfully loaded texture property for: " + skinFilename);
        return textures;
    }
}