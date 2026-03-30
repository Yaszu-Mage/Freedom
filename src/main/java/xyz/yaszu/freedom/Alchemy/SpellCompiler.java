package xyz.yaszu.freedom.Alchemy;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class SpellCompiler extends Util {

    /* ================= ENUMS ================= */

    public enum ritualkeywords {
        amplification, destruction, teleport, area, effect, location, range,
        regeneration, haste, speed, jump, poison, wither, strength, weakness
    }

    public enum ritualtype {
        destruction, teleport, area, effect
    }

    /* ================= LOGGER ================= */

    private static void log(String msg) {
        Freedom.get_plugin().getLogger().info("[SpellCompiler] " + msg);
    }

    /* ================= TOKENIZER ================= */

    enum TokenType { KEYWORD, NUMBER, IDENTIFIER }

    static class Token {
        TokenType type;
        String value;

        Token(TokenType t, String v) { type = t; value = v; }

        public String toString() { return type + ":" + value; }
    }

    static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();

        for (String word : input.toLowerCase().split(" ")) {
            if (word.matches("-?\\d+")) {
                tokens.add(new Token(TokenType.NUMBER, word));
            } else {
                try {
                    ritualkeywords.valueOf(word);
                    tokens.add(new Token(TokenType.KEYWORD, word));
                } catch (Exception e) {
                    tokens.add(new Token(TokenType.IDENTIFIER, word));
                }
            }
        }

        return tokens;
    }

    /* ================= AST ================= */

    static class SpellNode {
        List<StatementNode> statements = new ArrayList<>();
        List<Token> unusedTokens = new ArrayList<>();
    }

    static class StatementNode {
        ritualtype action;
        Location location;
        int range = 0;
        int amplification = 0;
        PotionEffect effect;
    }

    /* ================= PARSER ================= */

    static SpellNode parse(List<Token> tokens, Location base) {
        SpellNode spell = new SpellNode();
        StatementNode current = null;

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            boolean used = false;

            if (t.type == TokenType.KEYWORD) {
                ritualkeywords key = ritualkeywords.valueOf(t.value);

                if (isAction(key)) {
                    current = new StatementNode();
                    current.action = ritualtype.valueOf(key.name());
                    current.location = base;
                    spell.statements.add(current);
                    used = true;
                    continue;
                }

                if (current != null) {
                    switch (key) {

                        case amplification -> {
                            current.amplification++;
                            used = true;
                        }

                        case range -> {
                            if (i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.NUMBER) {
                                current.range = Integer.parseInt(tokens.get(++i).value);
                                used = true;
                            }
                        }

                        case location -> {
                            if (i + 3 < tokens.size()
                                    && tokens.get(i + 1).type == TokenType.NUMBER
                                    && tokens.get(i + 2).type == TokenType.NUMBER
                                    && tokens.get(i + 3).type == TokenType.NUMBER) {

                                double x = Double.parseDouble(tokens.get(++i).value);
                                double y = Double.parseDouble(tokens.get(++i).value);
                                double z = Double.parseDouble(tokens.get(++i).value);

                                current.location = new Location(base.getWorld(), x, y, z);
                            } else {
                                current.location = base;
                            }
                            used = true;
                        }

                        case regeneration, speed, strength, poison, wither, jump, haste -> {
                            current.effect = createEffect(key);
                            used = true;
                        }
                    }
                }
            }

            if (!used) spell.unusedTokens.add(t);
        }

        return spell;
    }

    static boolean isAction(ritualkeywords key) {
        return switch (key) {
            case teleport, destruction, area, effect -> true;
            default -> false;
        };
    }

    /* ================= VALIDATION ================= */

    static List<String> validate(SpellNode spell) {
        Set<String> errors = new HashSet<>();

        if (spell.statements.isEmpty()) {
            errors.add("No valid statements.");
        }

        for (Token t : spell.unusedTokens) {
            errors.add("Invalid token: " + t.value);
        }

        for (StatementNode stmt : spell.statements) {
            if (stmt.action == ritualtype.area && stmt.effect == null) {
                errors.add("Area requires effect.");
            }
        }

        return new ArrayList<>(errors);
    }

    /* ================= RITUAL SCAN ================= */

    static class RitualLayer {
        Map<Material, Integer> resources = new HashMap<>();
        List<ItemFrame> frames = new ArrayList<>();
    }

    static List<RitualLayer> scanLayers(Location center) {
        List<RitualLayer> layers = new ArrayList<>();
        World world = center.getWorld();

        for (int y = 0; y <= 15; y++) {
            RitualLayer layer = new RitualLayer();

            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {

                    Location base = center.clone().add(x, y, z);

                    if (base.getBlock().getType() == Material.DEEPSLATE_BRICKS) {

                        Location above = base.clone().add(0, 1, 0);

                        for (Entity e : world.getNearbyEntities(above, 0.5, 0.5, 0.5)) {
                            if (e instanceof ItemFrame frame) {
                                ItemStack item = frame.getItem();

                                if (item != null && item.getType() != Material.AIR) {
                                    layer.resources.merge(item.getType(), item.getAmount(), Integer::sum);
                                    layer.frames.add(frame);
                                }
                            }
                        }
                    }
                }
            }

            if (!layer.resources.isEmpty()) layers.add(layer);
        }

        return layers;
    }

    /* ================= COST ================= */

    static Map<Material, Integer> calculateCost(SpellNode spell, int layerCount) {
        Map<Material, Integer> cost = new HashMap<>();

        for (StatementNode stmt : spell.statements) {

            int base = 1 + stmt.range + (stmt.amplification * 2);



            base = (int) Math.ceil(base * 0.25);


            Material mat = switch (stmt.action) {
                case destruction -> Material.DIAMOND;
                case teleport -> Material.ENDER_PEARL;
                case area -> Material.REDSTONE;
                case effect -> Material.GLOWSTONE_DUST;
            };

            cost.merge(mat, base, Integer::sum);
        }
        log("Cost " + cost.toString());
        return cost;
    }

    static List<String> validateCost(Map<Material, Integer> cost, List<RitualLayer> layers) {

        // Copy actual frame contents (like a dry-run consume)
        Map<ItemFrame, Integer> simulated = new HashMap<>();

        for (RitualLayer layer : layers) {
            for (ItemFrame frame : layer.frames) {
                ItemStack item = frame.getItem();
                if (item != null && item.getType() != Material.AIR) {
                    simulated.put(frame, item.getAmount());
                }
            }
        }

        List<String> errors = new ArrayList<>();
        log("COST");
        log(cost.entrySet().toString());
        log("FUNCTION");
        for (var entry : cost.entrySet()) {
            Material mat = entry.getKey();
            int remaining = entry.getValue();
            log(remaining + " " + mat.name());
            for (var sim : simulated.entrySet()) {
                if (remaining <= 0) break;

                ItemFrame frame = sim.getKey();
                ItemStack item = frame.getItem();

                if (item == null || item.getType() != mat) continue;
                int available = sim.getValue();
                int take = Math.min(available, remaining);

                log("Available " + available + " Take " + take);

// FIX: subtract instead of wiping
                sim.setValue(available - take);

                remaining = remaining - take;

                log(remaining + " " + mat.name() + " left");
            }
            log(String.valueOf(remaining));
            if (remaining > 0) {
                log("Remaining within set " + remaining);
                errors.add("Missing " + mat.name() + " (" +
                        (entry.getValue() - remaining) + "/" + entry.getValue() + ")");
            }
        }

        return errors;
    }

    static void consumeResources(Map<Material, Integer> cost, List<RitualLayer> layers) {
        for (var entry : cost.entrySet()) {
            Material mat = entry.getKey();
            int remaining = entry.getValue();

            for (RitualLayer layer : layers) {
                if (remaining <= 0) break;

                for (ItemFrame frame : layer.frames) {
                    ItemStack item = frame.getItem();
                    if (item == null || item.getType() != mat) continue;

                    int take = Math.min(item.getAmount(), remaining);
                    item.setAmount(item.getAmount() - take);

                    frame.setItem(item.getAmount() <= 0 ? new ItemStack(Material.AIR) : item);

                    remaining -= take;
                    if (remaining <= 0) break;
                }
            }
        }
    }

    /* ================= EXECUTION ================= */

    static void execute(SpellNode spell, Player caster) {
        for (StatementNode stmt : spell.statements) {

            int power = stmt.range + stmt.amplification * 2;

            switch (stmt.action) {

                case teleport -> {
                    PortalParticleLifespan(caster.getLocation().clone().add(0,2,0), stmt.location.clone().add(caster.getLocation().clone().getDirection().multiply(2.5)));
                    PortalParticleLifespan(stmt.location,caster.getLocation().clone().add(0,2,4));
                }

                case destruction ->
                        stmt.location.getWorld().createExplosion(stmt.location, Math.max(1, power));

                case area -> {
                    for (Entity e : caster.getWorld().getNearbyEntities(stmt.location, power, power, power)) {
                        if (e instanceof LivingEntity l && stmt.effect != null) {
                            l.addPotionEffect(stmt.effect);
                        }
                    }
                }

                case effect -> {
                    if (stmt.effect != null) caster.addPotionEffect(stmt.effect);
                }
            }
        }
    }

    /* ================= EFFECT ================= */

    static PotionEffect createEffect(ritualkeywords key) {
        return switch (key) {
            case regeneration -> new PotionEffect(PotionEffectType.REGENERATION, 200, 1);
            case speed -> new PotionEffect(PotionEffectType.SPEED, 200, 1);
            case strength -> new PotionEffect(PotionEffectType.STRENGTH, 200, 1);
            case jump -> new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 1);
            case poison -> new PotionEffect(PotionEffectType.POISON, 200, 1);
            case wither -> new PotionEffect(PotionEffectType.WITHER, 200, 1);
            case haste -> new PotionEffect(PotionEffectType.HASTE, 200, 1);
            default -> null;
        };
    }

    /* ================= ENTRY ================= */

    public static boolean castSpell(String text, Location center, Player caster) {

        var tokens = tokenize(text);
        var ast = parse(tokens, center);

        var errors = validate(ast);
        if (!errors.isEmpty()) {
            errors.forEach(e -> caster.sendMessage("§c" + e));
            return true;
        }

        var layers = scanLayers(center);
        var cost = calculateCost(ast, layers.size());

        var costErrors = validateCost(cost, layers);
        if (!costErrors.isEmpty()) {
            costErrors.forEach(e -> caster.sendMessage("§c" + e));
            return true;
        }

        consumeResources(cost, layers);
        execute(ast, caster);

        return false;
    }
}