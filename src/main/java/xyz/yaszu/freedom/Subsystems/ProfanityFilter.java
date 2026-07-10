package xyz.yaszu.freedom.Subsystems;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfanityFilter {

    private final Set<String> blacklist = new HashSet<>();

    public ProfanityFilter() {
        loadDefaultBlacklist();
    }

    private void loadDefaultBlacklist() {
        blacklist.add("fuck");
        blacklist.add("balls");
        blacklist.add("shit");
        blacklist.add("faggot");
        blacklist.add("cunt");
        blacklist.add("nigger");
        blacklist.add("nigga");
        blacklist.add("penis");
        blacklist.add("vagina");
        blacklist.add("arse");
        blacklist.add("arsehead");
        blacklist.add("arsehole");
        blacklist.add("asshole");
        blacklist.add("bastard");
        blacklist.add("bitch");
        blacklist.add("bloddy");
        blacklist.add("bugger");
        blacklist.add("chigga");
        blacklist.add("child-fucker");
        blacklist.add("cock");
        blacklist.add("cocksucker");
        blacklist.add("crap");
        blacklist.add("cunt");
        blacklist.add("dammit");
        blacklist.add("damn");
        blacklist.add("puta");
        blacklist.add("damned");
        blacklist.add("dick");
        blacklist.add("fag");
        blacklist.add("dick-head");
        blacklist.add("tit");
        blacklist.add("titties");
        blacklist.add("dickhead");
        blacklist.add("dyke");
        blacklist.add("hell");
    }

    public String cleanText(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String cleanedText = input;

        for (String swearWord : blacklist) {
            String regex = "(?i)\\b" + Pattern.quote(swearWord) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(cleanedText);
            if (matcher.find()) {
                String mask = "*".repeat(swearWord.length());
                cleanedText = matcher.replaceAll(mask);
            }
        }
        return cleanedText;
    }

}
