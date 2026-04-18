package xyz.yaszu.freedom.Util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CopypartyClient {

    public static final String DEFAULT_LISTING_URL = "https://files.yaszu.xyz/skins/?json";
    public static final String DEFAULT_FILES_BASE_URL = "https://files.yaszu.xyz/skins/";

    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern HREF_PATTERN = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private final HttpClient client;

    public CopypartyClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build());
    }

    public CopypartyClient(HttpClient client) {
        this.client = client;
    }

    public String fetchRandomSkinOrFallback(List<String> fallbackSkins) {
        return fetchRandomSkinOrFallback(DEFAULT_LISTING_URL, DEFAULT_FILES_BASE_URL, fallbackSkins, new Random());
    }

    public String fetchRandomSkinOrFallback(String listingUrl, String filesBaseUrl, List<String> fallbackSkins, Random random) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(listingUrl))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return selectRandomSkinFromJson(response.body(), filesBaseUrl, fallbackSkins, random);
            }
        } catch (IOException | InterruptedException | IllegalArgumentException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        return chooseRandom(fallbackSkins, random)
                .orElseThrow(() -> new IllegalStateException("No skins available from remote source or fallback list."));
    }

    static String selectRandomSkinFromJson(String jsonBody, String filesBaseUrl, List<String> fallbackSkins, Random random) {
        List<String> remoteSkins = extractSkinUrls(jsonBody, filesBaseUrl);
        if (!remoteSkins.isEmpty()) {
            return remoteSkins.get(random.nextInt(remoteSkins.size()));
        }

        return chooseRandom(fallbackSkins, random)
                .orElseThrow(() -> new IllegalStateException("No skins available from remote JSON or fallback list."));
    }

    static List<String> extractSkinUrls(String jsonBody, String filesBaseUrl) {
        if (jsonBody == null || jsonBody.isBlank()) {
            return Collections.emptyList();
        }

        String normalizedBase = filesBaseUrl.endsWith("/") ? filesBaseUrl : filesBaseUrl + "/";
        LinkedHashSet<String> urls = new LinkedHashSet<>();

        Matcher nameMatcher = NAME_PATTERN.matcher(jsonBody);
        while (nameMatcher.find()) {
            String fileName = unescapeJsonString(nameMatcher.group(1));
            addIfSupported(urls, normalizedBase, fileName);
        }

        // files.yaszu.xyz may return an HTML listing despite ?json (proxy/challenge path);
        // parse anchor hrefs as a secondary source so NPC skin selection still works.
        Matcher hrefMatcher = HREF_PATTERN.matcher(jsonBody);
        while (hrefMatcher.find()) {
            addIfSupported(urls, normalizedBase, hrefMatcher.group(1));
        }

        return new ArrayList<>(urls);
    }

    private static void addIfSupported(LinkedHashSet<String> urls, String filesBaseUrl, String candidatePath) {
        if (candidatePath == null || candidatePath.isBlank()) {
            return;
        }

        String cleanPath = stripQueryAndFragment(candidatePath.trim());
        if (!isSupportedSkinFile(cleanPath)) {
            return;
        }

        URI baseUri = URI.create(filesBaseUrl);
        String relative = toRelativePath(cleanPath, baseUri.getPath());
        urls.add(baseUri.resolve(relative).toString());
    }

    private static String toRelativePath(String path, String basePath) {
        String relative = path.startsWith("/") ? path.substring(1) : path;
        String normalizedBasePath = basePath == null ? "" : basePath;

        if (normalizedBasePath.startsWith("/")) {
            normalizedBasePath = normalizedBasePath.substring(1);
        }
        if (!normalizedBasePath.isEmpty() && !normalizedBasePath.endsWith("/")) {
            normalizedBasePath = normalizedBasePath + "/";
        }

        if (!normalizedBasePath.isEmpty() && relative.startsWith(normalizedBasePath)) {
            relative = relative.substring(normalizedBasePath.length());
        }

        return relative;
    }

    private static Optional<String> chooseRandom(List<String> skins, Random random) {
        if (skins == null || skins.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(skins.get(random.nextInt(skins.size())));
    }

    private static boolean isSupportedSkinFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    private static String stripQueryAndFragment(String path) {
        int queryIndex = path.indexOf('?');
        int hashIndex = path.indexOf('#');
        int cutIndex = -1;

        if (queryIndex >= 0 && hashIndex >= 0) {
            cutIndex = Math.min(queryIndex, hashIndex);
        } else if (queryIndex >= 0) {
            cutIndex = queryIndex;
        } else if (hashIndex >= 0) {
            cutIndex = hashIndex;
        }

        return cutIndex >= 0 ? path.substring(0, cutIndex) : path;
    }

    private static String unescapeJsonString(String value) {
        return value
                .replace("\\\\/", "/")
                .replace("\\\\\"", "\"")
                .replace("\\\\", "\\");
    }
}