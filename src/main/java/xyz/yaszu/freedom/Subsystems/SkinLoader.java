package xyz.yaszu.freedom.Subsystems;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class SkinLoader {

    // Simple Pair class to replace ICU dependency
    public static class Pair<A, B> {
        public final A first;
        public final B second;

        private Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public static <A, B> Pair<A, B> of(A first, B second) {
            return new Pair<>(first, second);
        }
    }

    public static class SkinResult {
        public final List<TextureProperty> textures;
        public final String filename;
        public final String value;
        public final String signature;

        public SkinResult(List<TextureProperty> textures, String filename, String value, String signature) {
            this.textures = textures;
            this.filename = filename;
            this.value = value;
            this.signature = signature;
        }
    }
    private static final String BASE_URL = "https://files.yaszu.xyz/skins/";
    private static final String MINESKIN_URL = "https://api.mineskin.org/generate/url";
    private static final String USER_AGENT = "Freedom/1.0";
    private static final Random random = new Random();
    private static final java.util.Map<String, SkinResult> SKIN_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicLong MINESKIN_NEXT_ALLOWED_MS = new java.util.concurrent.atomic.AtomicLong(0L);

    public static SkinResult loadRandomSkin() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?ls"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String skinFilename = "";
            String responseBody = response.body().trim();
            if (responseBody.isEmpty()) {
                System.err.println("Empty skin list response from: " + BASE_URL);
                return null;
            }

            // First, try to parse as JSON
            try {
                com.google.gson.JsonElement jsonElement = com.google.gson.JsonParser.parseString(responseBody);

                if (jsonElement.isJsonArray()) {
                    // SCENARIO A/B: Handle array response
                    com.google.gson.JsonArray jsonArray = jsonElement.getAsJsonArray();
                    if (!jsonArray.isEmpty()) {
                        com.google.gson.JsonElement randomElement = jsonArray.get(random.nextInt(jsonArray.size()));
                        if (randomElement.isJsonPrimitive()) {
                            // Array of strings
                            skinFilename = randomElement.getAsString();
                        } else if (randomElement.isJsonObject()) {
                            // Array of objects with "p" field
                            com.google.gson.JsonObject obj = randomElement.getAsJsonObject();
                            if (obj.has("p")) {
                                skinFilename = obj.get("p").getAsString();
                            }
                        }
                    }
                } else if (jsonElement.isJsonObject()) {
                    // SCENARIO C: Handle object response with nested list
                    com.google.gson.JsonObject jsonObject = jsonElement.getAsJsonObject();

                    if (jsonObject.has("files") && jsonObject.get("files").isJsonArray()) {
                        skinFilename = pickFromFilesArray(jsonObject.getAsJsonArray("files"));
                    }

                    // Try to find an array in the object
                    if (skinFilename.isEmpty()) {
                        for (String key : jsonObject.keySet()) {
                            com.google.gson.JsonElement value = jsonObject.get(key);
                            if (value.isJsonArray()) {
                                com.google.gson.JsonArray array = value.getAsJsonArray();
                                if (!array.isEmpty()) {
                                    com.google.gson.JsonElement randomElement = array.get(random.nextInt(array.size()));
                                    if (randomElement.isJsonPrimitive()) {
                                        skinFilename = randomElement.getAsString();
                                    } else if (randomElement.isJsonObject()) {
                                        com.google.gson.JsonObject itemObj = randomElement.getAsJsonObject();
                                        if (itemObj.has("p")) {
                                            skinFilename = itemObj.get("p").getAsString();
                                        }
                                    }
                                    if (!skinFilename.isEmpty()) break;
                                }
                            }
                        }
                    }
                } else if (jsonElement.isJsonPrimitive()) {
                    // SCENARIO D: Handle simple string response
                    skinFilename = jsonElement.getAsString();
                }
            } catch (com.google.gson.JsonSyntaxException jsonEx) {
                skinFilename = pickFromPlainList(responseBody);
            } catch (Exception e) {
                System.err.println("Failed to parse skin list response: " + responseBody);
                e.printStackTrace();
                return null;
            }

            if (skinFilename.isEmpty()) {
                skinFilename = pickFromPlainList(responseBody);
            }

            if (skinFilename.isEmpty()) {
                System.out.println("No skins found on the server.");
                return null;
            }

            return loadSignedSkinFromFilename(skinFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SkinResult loadSignedSkinFromFilename(String skinFilename) {
        if (skinFilename == null || skinFilename.isEmpty()) {
            return null;
        }
        String skinUrl = BASE_URL + skinFilename;
        SkinResult cached = SKIN_CACHE.get(skinUrl);
        if (cached != null) {
            return cached;
        }
        SkinResult result = fetchSignedSkinFromUrl(skinUrl, skinFilename);
        if (result != null) {
            SKIN_CACHE.put(skinUrl, result);
        }
        return result;
    }

    public static List<TextureProperty> buildSignedTextures(String value, String signature) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        List<TextureProperty> textures = new ArrayList<>();
        textures.add(new TextureProperty("textures", value, signature));
        return textures;
    }

    private static SkinResult fetchSignedSkinFromUrl(String skinUrl, String filename) {
        long now = System.currentTimeMillis();
        long nextAllowed = MINESKIN_NEXT_ALLOWED_MS.get();
        if (now < nextAllowed) {
            System.err.println("MineSkin rate-limited. Skipping request for: " + filename);
            return null;
        }
        try {
            String payload = "{\"url\":\"" + skinUrl + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MINESKIN_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", USER_AGENT)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (response.statusCode() == 429) {
                    long delayMs = parseMineSkinDelayMs(response.body());
                    if (delayMs > 0) {
                        MINESKIN_NEXT_ALLOWED_MS.set(System.currentTimeMillis() + delayMs);
                    } else {
                        MINESKIN_NEXT_ALLOWED_MS.set(System.currentTimeMillis() + 5000);
                    }
                }
                System.err.println("MineSkin request failed: " + response.statusCode() + " body=" + response.body());
                return null;
            }
            com.google.gson.JsonElement root = com.google.gson.JsonParser.parseString(response.body());
            if (!root.isJsonObject()) {
                System.err.println("MineSkin response not an object: " + response.body());
                return null;
            }
            if (root.getAsJsonObject().has("delayInfo")) {
                long delayMs = parseMineSkinDelayMs(response.body());
                if (delayMs > 0) {
                    MINESKIN_NEXT_ALLOWED_MS.set(System.currentTimeMillis() + delayMs);
                }
            }
            com.google.gson.JsonObject data = root.getAsJsonObject().getAsJsonObject("data");
            if (data == null) {
                System.err.println("MineSkin response missing data: " + response.body());
                return null;
            }
            com.google.gson.JsonObject texture = data.getAsJsonObject("texture");
            if (texture == null) {
                System.err.println("MineSkin response missing texture: " + response.body());
                return null;
            }
            String value = texture.has("value") ? texture.get("value").getAsString() : "";
            String signature = texture.has("signature") ? texture.get("signature").getAsString() : "";
            if (value.isEmpty()) {
                System.err.println("MineSkin response missing texture value: " + response.body());
                return null;
            }
            List<TextureProperty> textures = buildSignedTextures(value, signature);
            System.out.println("Loaded MineSkin textures for: " + filename);
            return new SkinResult(textures, filename, value, signature);
        } catch (Exception e) {
            System.err.println("MineSkin request failed for: " + skinUrl);
            e.printStackTrace();
            return null;
        }
    }

    private static long parseMineSkinDelayMs(String responseBody) {
        try {
            com.google.gson.JsonElement root = com.google.gson.JsonParser.parseString(responseBody);
            if (!root.isJsonObject()) {
                return 0L;
            }
            com.google.gson.JsonObject obj = root.getAsJsonObject();
            if (obj.has("delayInfo")) {
                com.google.gson.JsonObject delayInfo = obj.getAsJsonObject("delayInfo");
                if (delayInfo != null) {
                    if (delayInfo.has("millis")) {
                        return delayInfo.get("millis").getAsLong();
                    }
                    if (delayInfo.has("seconds")) {
                        return delayInfo.get("seconds").getAsLong() * 1000L;
                    }
                }
            }
            if (obj.has("nextRequest")) {
                return obj.get("nextRequest").getAsLong() * 1000L;
            }
        } catch (Exception ignored) {
            // Best-effort parsing only.
        }
        return 0L;
    }

    private static String pickFromFilesArray(com.google.gson.JsonArray filesArray) {
        List<String> candidates = new ArrayList<>();
        for (com.google.gson.JsonElement element : filesArray) {
            if (!element.isJsonObject()) {
                continue;
            }
            com.google.gson.JsonObject obj = element.getAsJsonObject();
            if (obj.has("href") && obj.get("href").isJsonPrimitive()) {
                String href = obj.get("href").getAsString().trim();
                if (!href.isEmpty()) {
                    candidates.add(href);
                }
            }
        }
        if (candidates.isEmpty()) {
            return "";
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private static String pickFromPlainList(String responseBody) {
        String[] lines = responseBody.split("\\r?\\n");
        List<String> candidates = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
                    trimmed = trimmed.substring(1, trimmed.length() - 1);
                }
                candidates.add(trimmed);
            }
        }
        if (candidates.isEmpty()) {
            return "";
        }
        return candidates.get(random.nextInt(candidates.size()));
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