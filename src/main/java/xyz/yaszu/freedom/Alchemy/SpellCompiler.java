package xyz.yaszu.freedom.Alchemy;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class SpellCompiler extends Util {

    /* ================= ENUMS ================= */

    public enum ritualkeywords {
        amplification, destruction, teleport, area, effect, location, range,
        regeneration, haste, speed, jump, poison, wither, strength, weakness,
        rain, sun, thundering, day, night,shock,delay,goon,sixtyseven,nothing
    }

    public enum ritualtype {
        destruction, teleport, area, effect,rain,sun,thundering,day,night,shock,goon,sixtyseven,nothing
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
        int delay = 0;
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
                        case amplification -> { current.amplification++; used = true; }
                        case range -> {
                            if (i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.NUMBER) {
                                current.range = Math.min(25,Integer.parseInt(tokens.get(++i).value));
                                used = true;
                            }
                        }
                        case delay -> {
                            if (i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.NUMBER) {
                                current.delay = Integer.parseInt(tokens.get(++i).value) * 20;
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
            case teleport, destruction, area, effect, thundering, rain, sun, day, night, shock,goon,sixtyseven,nothing-> true;
            default -> false;
        };
    }

    /* ================= VALIDATION ================= */

    static List<String> validate(SpellNode spell) {
        Set<String> errors = new HashSet<>();
        if (spell.statements.isEmpty()) errors.add("No valid statements.");
        for (Token t : spell.unusedTokens) errors.add("Invalid token: " + t.value);
        for (StatementNode stmt : spell.statements) {
            if (stmt.action == ritualtype.area && stmt.effect == null) errors.add("Area requires effect.");
        }
        return new ArrayList<>(errors);
    }

    /* ================= UNIVERSAL ECONOMY SYSTEM ================= */

    /**
     * Assigns a power value to every material in Minecraft.
     */
    private static int getItemValue(Material mat) {
        return switch (mat) {
            case PLAYER_HEAD -> 15000;
            case DRAGON_EGG -> 10000;
            case ENCHANTED_GOLDEN_APPLE -> 7500;
            case NETHER_STAR -> 2500;
            case DEEPSLATE_COAL_ORE -> 2000;
            case NETHERITE_INGOT -> 750;
            case DIAMOND, EMERALD -> 600;
            case DRAGON_BREATH -> 500;
            case GOLDEN_APPLE -> 250;
            case ENCHANTED_BOOK -> 200;
            case GOLD_INGOT -> 25;
            case ENDER_PEARL -> 20;
            case IRON_INGOT -> 10;
            case LAPIS_LAZULI -> 8;
            case REDSTONE, GLOWSTONE_DUST -> 5;
            case COAL -> 2;
            default -> {
                if (mat.isBlock() && mat.isSolid()) yield 1;
                if (mat.isEdible()) yield 3;
                yield 2; // Default for miscellaneous items
            }
        };
    }

    static int calculateTotalPowerCost(SpellNode spell) {
        int totalRequiredPower = 0;
        for (StatementNode stmt : spell.statements) {
            // Base complexity: Range and Amp increase cost exponentially
            int complexity = 1 + stmt.range + (stmt.amplification * 5);
            log("Complexity " + String.valueOf(complexity));
            int actionBase = switch (stmt.action) {
                case destruction -> 500;
                case teleport -> 50;
                case area -> 40;
                case effect -> 25;
                case sun, thundering, rain, shock -> 45;
                case day,night -> 30;
                case goon, sixtyseven -> 1000;
                case nothing -> 750;
            };
            log("Action Base " + String.valueOf(actionBase));
            totalRequiredPower += (complexity * actionBase);
            log("Total " + String.valueOf(totalRequiredPower));
        }

        return totalRequiredPower;
    }

    /* ================= RITUAL SCAN ================= */

    static class RitualLayer {
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
                                if (frame.getItem().getType() != Material.AIR) layer.frames.add(frame);
                            }
                        }
                    }
                }
            }
            if (!layer.frames.isEmpty()) layers.add(layer);
        }
        return layers;
    }

    /* ================= COST VALIDATION & CONSUMPTION ================= */

    static List<String> validateTotalCost(int powerRequired, List<RitualLayer> layers) {
        int totalProvided = 0;
        for (RitualLayer layer : layers) {
            for (ItemFrame frame : layer.frames) {
                ItemStack item = frame.getItem();
                totalProvided += (getItemValue(item.getType()) * item.getAmount());
            }
        }

        if (totalProvided < powerRequired) {
            return List.of("Insufficient Material. Provided: " + totalProvided + " / Required: " + powerRequired);
        }
        return new ArrayList<>();
    }

    static void consumePowerResources(int powerRequired, List<RitualLayer> layers) {
        int debt = powerRequired;
        for (RitualLayer layer : layers) {
            if (debt <= 0) break;
            for (ItemFrame frame : layer.frames) {
                ItemStack item = frame.getItem();
                if (item.getType() == Material.AIR) continue;

                int val = getItemValue(item.getType());
                int amountNeeded = (int) Math.ceil((double) debt / val);
                int toTake = Math.min(item.getAmount(), amountNeeded);

                debt -= (toTake * val);
                int remaining = item.getAmount() - toTake;
                frame.setItem(remaining <= 0 ? new ItemStack(Material.AIR) : new ItemStack(item.getType(), remaining));

                if (debt <= 0) break;
            }
        }
    }

    /* ================= EXECUTION ================= */

    static void execute(SpellNode spell, Player caster) {
        for (StatementNode stmt : spell.statements) {
            int power = stmt.range + stmt.amplification * 2;
            new BukkitRunnable() {
                @Override
                public void run() {
                        switch (stmt.action) {
                            case teleport -> {
                                PortalParticleLifespan(caster.getLocation().clone().add(0,2,4).setRotation(0,0), stmt.location.clone().add(0,0,4));
                                PortalParticleLifespan(stmt.location.clone().add(0,2,0),caster.getLocation().clone().add(0,2,4).setRotation(0,0));
                            }
                            case destruction -> {
                                createRemoteExplosionParticles(stmt.location,15,stmt.range);
                            }
                            case area -> {
                                for (Entity e : caster.getWorld().getNearbyEntities(stmt.location, power, power, power)) {
                                    if (e instanceof LivingEntity l && stmt.effect != null) l.addPotionEffect(stmt.effect);

                                }
                            }
                            case effect -> {
                                if (stmt.effect != null) caster.addPotionEffect(stmt.effect);
                            }
                            case rain -> {
                                caster.getWorld().setStorm(true);
                                caster.getWorld().setThundering(false);
                                caster.getWorld().setWeatherDuration(6000 * Math.min(1,stmt.amplification));
                            }
                            case sun -> {
                                caster.getWorld().setStorm(false);
                                caster.getWorld().setThundering(false);
                                caster.getWorld().setWeatherDuration(0);
                            }
                            case thundering -> {
                                caster.getWorld().setThundering(true);
                                caster.getWorld().setWeatherDuration(6000 * Math.min(1,stmt.amplification));
                                caster.getWorld().setStorm(true);
                            }
                            case day -> {
                                caster.getWorld().setTime(1000);
                            }
                            case night -> {
                                caster.getWorld().setTime(13000);
                            }
                            case shock -> {
                                if (stmt.range > 0) {
                                    for (int x = 0; x < stmt.range; x++) {
                                        stmt.location.getWorld().strikeLightning(stmt.location);
                                    }
                                } else {
                                    stmt.location.getWorld().strikeLightning(stmt.location);
                                }
                            }
                            case goon -> {
                                for (ItemStack item : caster.getInventory().getContents()) {
                                    if (item != null) {
                                        if ((item.getType() != Material.WRITABLE_BOOK) && (item.getType() != Material.WRITTEN_BOOK) && (item.getType() != Material.MILK_BUCKET) && (item.getType() != Material.BOOK)) {
                                            caster.getInventory().remove(item);
                                            caster.getWorld().dropItemNaturally(caster.getLocation(),ItemStack.of(Material.MILK_BUCKET));
                                        }

                                    }
                                }
                                caster.getLocation().getWorld().spawnParticle(Particle.ITEM_SNOWBALL,caster.getLocation(),100,0,0,0,0.1);
                                caster.damage(10000,caster);
                            }
                            case sixtyseven -> {
                                caster.getWorld().strikeLightning(caster.getLocation());
                                caster.damage(10000,caster);
                            }
                            case nothing -> {
                                //the void
                                if (stmt.range > 0 && stmt.amplification > 1 && caster.getLocation().getWorld() == Bukkit.getWorld("world")) {
                                    Location voidloc = stmt.location.clone().set(0,-60,0);
                                    voidloc.setWorld(Bukkit.getWorld("void"));
                                    PortalParticleLifespan(caster.getLocation().clone().add(0,2,4).setRotation(0,0), voidloc.clone().add(0,0,4),new Particle.DustOptions(Color.RED,8f));
                                    PortalParticleLifespan(voidloc,caster.getLocation().clone().add(0,2,0).setRotation(0,0),new Particle.DustOptions(Color.RED,8f));
                                } else {
                                    Location voidloc = Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation().clone().add(0,2,0);

                                    PortalParticleLifespan(caster.getLocation().clone().add(0,2,4).setRotation(0,0), voidloc.clone().add(0,0,4),new Particle.DustOptions(Color.RED,8f));
                                    PortalParticleLifespan(voidloc,caster.getLocation().clone().add(0,2,0).setRotation(0,0),new Particle.DustOptions(Color.RED,8f));
                                }
                            }
                        }
                    }

            }.runTaskLater(Freedom.get_plugin(),stmt.delay);


        }
    }

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

    public static int castSpell(String text, Location center, Player caster) {
        var tokens = tokenize(text);
        var ast = parse(tokens, center);

        var errors = validate(ast);
        if (!errors.isEmpty()) {
            errors.forEach(e -> caster.sendMessage("§c" + e));
            return 0;
        }

        var layers = scanLayers(center);
        int powerRequired = calculateTotalPowerCost(ast);

        var costErrors = validateTotalCost(powerRequired, layers);
        if (!costErrors.isEmpty()) {
            costErrors.forEach(e -> caster.sendMessage("§c" + e));
            return 0;
        }

        consumePowerResources(powerRequired, layers);
        execute(ast, caster);
        caster.sendMessage("§aSpell cast successfully! (" + powerRequired + " material consumed)");

        return powerRequired;
    }
    public static int cost(String text, Player caster) {
        var tokens = tokenize(text);
        var ast = parse(tokens, caster.getLocation());

        var errors = validate(ast);
        if (!errors.isEmpty()) {
            errors.forEach(e -> caster.sendMessage("§c" + e));
            return 0;
        }
        int powerRequired = calculateTotalPowerCost(ast);

        caster.sendActionBar("§aThis spell costs (" + powerRequired + ")!");

        return powerRequired;
    }
}