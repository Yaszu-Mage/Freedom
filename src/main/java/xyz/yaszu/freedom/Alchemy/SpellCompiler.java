package xyz.yaszu.freedom.Alchemy;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Subsystems.AdminManager;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

import static xyz.yaszu.freedom.Subsystems.CurrencyManager.*;

public class SpellCompiler extends Util {

    /* ================= ENUMS ================= */



    public enum ritualkeywords {
        amplification, destruction, teleport, area, effect, location, range,
        regeneration, haste, speed, jump, poison, wither, strength, weakness,
        rain, sun, thundering, day, night, shock, delay, goon, sixtyseven,nothing,
        fire,water,earth,air,soul,blast,moveset,province,up,down,look,vector,point
    }


    public List<ritualkeywords> blockedTypesforMobile = List.of(ritualkeywords.destruction,ritualkeywords.teleport,ritualkeywords.location);


    public enum ritualtype {
        destruction(750), teleport(5000), area(40), effect(25),
        rain(45), sun(450), thundering(450), day(300), night(300),
        shock(45), goon(1000), sixtyseven(1000), nothing(750),blast(500),moveset(250),province(1000),
        vector(45);

        private final int baseCost;
        ritualtype(int baseCost) { this.baseCost = baseCost; }
        public int getBaseCost() { return baseCost; }

        public void execute(StatementNode stmt, Player caster) {
            int power = stmt.range + stmt.amplification * 2;
            if (stmt.location == null) stmt.location = caster.getLocation();
            switch (this) {
                case vector -> {
                    if (stmt.pointA != null && stmt.pointB != null) {
                        double distance = stmt.pointA.distance(stmt.pointB);
                        Location midpoint = Util.getMidpoint(stmt.pointA, stmt.pointB);
                        Location shift = caster.getLocation().clone().add(midpoint);
                        shift.getNearbyLivingEntities(distance/2).forEach(living -> living.setVelocity(directionTo(stmt.pointA, stmt.pointB).multiply(power)));
                    }
                }
                case teleport -> {

                    PortalParticleLifespan(caster.getLocation().clone().add(0,2,4).setRotation(0,0), stmt.location.clone().add(0,0,4));
                    PortalParticleLifespan(stmt.location.clone().add(0,2,0),caster.getLocation().clone().add(0,2,4).setRotation(0,0));
                }
                case destruction -> {
                    createRemoteExplosionParticles(stmt.location,15,power);
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
                    caster.getWorld().setWeatherDuration(6000 * Math.max(1,stmt.amplification));
                }
                case sun -> {
                    caster.getWorld().setStorm(false);
                    caster.getWorld().setThundering(false);
                    caster.getWorld().setWeatherDuration(0);
                }
                case thundering -> {
                    caster.getWorld().setThundering(true);
                    caster.getWorld().setWeatherDuration(6000 * Math.max(1,stmt.amplification));
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
                case blast -> {
                    Location loc;
                    loc = Objects.requireNonNullElseGet(stmt.location, caster::getLocation).clone();
                    switch (stmt.direction) {
                        case up -> {
                            loc.setRotation(0,90);
                        }
                        case down -> {
                            loc.setRotation(0,-90);
                        }
                        default -> {
                            loc.setRotation(caster.getLocation().getYaw(),caster.getLocation().getPitch());
                        }
                    }
                    double pitch = loc.getPitch();
                    //get direction
                    if (pitch >= 90) {
                        createMinMagicCircle(loc,15,getSoulType(caster));
                    } else if (pitch <= -90) {
                        createMinMagicCircle(loc.add(0,2,0),15,getSoulType(caster));
                    } else {
                        createVerticleMinMagicCircle(loc.clone().add(0,4,0),15,getSoulType(caster),caster.getYaw() - 90,loc,100,0.4);
                        //vertical...
                    }
                    ritualkeywords element = stmt.elements.isEmpty() ? ritualkeywords.air : stmt.elements.iterator().next();
                    switch (element) {
                        case fire -> {
                            //flamethrower - launch fireballs in direction
                            Vector direction = loc.getDirection();
                            for (int i = 0; i < power; i++) {
                                Fireball fireball = loc.getWorld().spawn(loc.clone().add(direction.clone().multiply(2)), Fireball.class);
                                fireball.setVelocity(direction.multiply(1.5 + (power / 10.0)));
                                fireball.setYield(Math.min(4.0f, power / 10.0f));
                            }
                            loc.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1, 0.8f);
                        }
                        case air -> {
                            //fling up
                            caster.setVelocity(caster.getVelocity().add(new Vector(0, 1 + (power / 10.0), 0)));
                            caster.setVelocity(loc.getDirection().multiply(-(2 + (power / 2))));
                            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1.2f);
                        }
                        case water -> {
                            //water blast - push entities in direction
                            Vector direction = loc.getDirection();
                            for (Entity e : loc.getWorld().getNearbyEntities(loc, power, power, power)) {
                                if (e instanceof LivingEntity le && e != caster) {
                                    le.setVelocity(direction.multiply(1 + (power / 5.0)));
                                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100 + (power * 10), 1));
                                }
                            }
                            loc.getWorld().playSound(loc, Sound.BLOCK_WATER_AMBIENT, 1, 0.9f);
                        }
                        case earth -> {
                            //throw rocks - summon falling blocks in direction
                            Vector direction = loc.getDirection();
                            for (int i = 0; i < Math.max(1, power / 5); i++) {
                                Location rockLoc = loc.clone().add(direction.clone().multiply(2 + i));
                                FallingBlock rock = loc.getWorld().spawnFallingBlock(rockLoc, Material.STONE.createBlockData());
                                rock.setVelocity(direction.multiply(1.2 + (power / 15.0)));
                                rock.setDropItem(false);
                            }
                            loc.getWorld().playSound(loc, Sound.BLOCK_STONE_BREAK, 1, 0.8f);
                        }
                        case poison -> {
                            //green gas - apply poison effect to nearby entities
                            for (Entity e : loc.getWorld().getNearbyEntities(loc, power, power, power)) {
                                if (e instanceof LivingEntity le && e != caster) {
                                    le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100 + (power * 5), Math.min(4, power / 5)));
                                }
                            }
                            for (int i = 0; i < 20 + power; i++) {
                                Location particleLoc = loc.clone().add(
                                    (Math.random() - 0.5) * power,
                                    (Math.random() - 0.5) * power / 2,
                                    (Math.random() - 0.5) * power
                                );
                                loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 3, 0, 0, 0, new Particle.DustOptions(Color.GREEN, 1.5f));
                            }
                            loc.getWorld().playSound(loc, Sound.ENTITY_WITCH_DRINK, 1, 0.8f);
                        }
                        case wither -> {
                            //black gas - apply wither effect to nearby entities
                            for (Entity e : loc.getWorld().getNearbyEntities(loc, power, power, power)) {
                                if (e instanceof LivingEntity le && e != caster) {
                                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100 + (power * 5), Math.min(4, power / 5)));
                                }
                            }
                            for (int i = 0; i < 20 + power; i++) {
                                Location particleLoc = loc.clone().add(
                                    (Math.random() - 0.5) * power,
                                    (Math.random() - 0.5) * power / 2,
                                    (Math.random() - 0.5) * power
                                );
                                loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 3, 0, 0, 0, new Particle.DustOptions(Color.BLACK, 1.5f));
                            }
                            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1, 0.9f);
                        }
                    }
                }
                case moveset -> {
                    List<String> elementNames = stmt.elements.stream()
                            .map(Enum::name)
                            .sorted()
                            .toList();
                    String elements = String.join(",", elementNames);
                    String movesetId = "elements:" + elements + ";amp:" + stmt.amplification + ";range:" + stmt.range;
                    caster.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.moveset(), org.bukkit.persistence.PersistentDataType.STRING, movesetId);
                    caster.sendMessage("§aYour moveset has been set to: " + movesetId);
                }
                case province -> {

                    // Logic handled in Alchemy.java or after execution
                }
            }
        }
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

    public static List<Token> tokenize(String input) {
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
        ritualkeywords direction = ritualkeywords.down;
        Set<ritualkeywords> elements = new HashSet<>();
        Player caster;
        ritualtype action;
        Location pointA;
        Location pointB;
        Location location;
        int range = 0;
        int amplification = 0;
        PotionEffect effect;
        int delay = 0;
    }

    /* ================= PARSER ================= */

    public static SpellNode parse(List<Token> tokens, Location base,Player caster) {
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
                    current.location = base.clone();
                    spell.statements.add(current);
                    current.caster = caster;
                    used = true;
                } else if (current != null) {
                    switch (key) {
                        case point -> {
                            if (current.pointA == null) {
                                if (i + 3 < tokens.size()
                                        && tokens.get(i + 1).type == TokenType.NUMBER
                                        && tokens.get(i + 2).type == TokenType.NUMBER
                                        && tokens.get(i + 3).type == TokenType.NUMBER) {
                                    double x = Double.parseDouble(tokens.get(++i).value);
                                    double y = Double.parseDouble(tokens.get(++i).value);
                                    double z = Double.parseDouble(tokens.get(++i).value);
                                    current.pointA = new Location(base.getWorld(), x, y, z);
                                    used = true;
                                }
                            } else {
                                if (i + 3 < tokens.size()
                                        && tokens.get(i + 1).type == TokenType.NUMBER
                                        && tokens.get(i + 2).type == TokenType.NUMBER
                                        && tokens.get(i + 3).type == TokenType.NUMBER) {
                                    double x = Double.parseDouble(tokens.get(++i).value);
                                    double y = Double.parseDouble(tokens.get(++i).value);
                                    double z = Double.parseDouble(tokens.get(++i).value);
                                    current.pointB = new Location(base.getWorld(), x, y, z);
                                    used = true;
                                }
                            }
                        }
                        case up,down,look -> {
                            current.direction = key;
                            used = true;
                        }
                        case fire,water,earth,air,soul,poison,wither -> {
                            current.elements.add(key);
                            used = true;
                        }
                        case amplification -> { current.amplification++; used = true; }
                        case range -> {
                            if (i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.NUMBER) {
                                current.range = Math.min(25, Integer.parseInt(tokens.get(++i).value));
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
                                used = true;
                            }
                        }
                        case regeneration, speed, strength, jump, haste -> {
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
            case teleport, destruction, area, effect, thundering, rain, sun, day, night, shock,goon,sixtyseven,nothing,blast,moveset,province-> true;
            default -> false;
        };
    }

    /* ================= VALIDATION ================= */

    public static List<String> validate(SpellNode spell) {
        Set<String> errors = new LinkedHashSet<>();
        if (spell.statements.isEmpty()) errors.add("No valid statements found. Did you start with an action keyword?");
        for (Token t : spell.unusedTokens) errors.add("Unrecognized or misplaced token: " + t.value);
        for (StatementNode stmt : spell.statements) {
            if (stmt.action == ritualtype.area && stmt.effect == null) {
                errors.add("Action 'area' requires an effect (e.g., speed, strength, poison).");
            }
        }
        return new ArrayList<>(errors);
    }

    /* ================= UNIVERSAL ECONOMY SYSTEM ================= */

    /**
     * Assigns a power value to every material in Minecraft.
     */
    public static int getItemValue(Material mat) {
        return getItemValue(mat, false);
    }

    public static int getItemValue(Material mat, boolean isMobile) {
        if (isMobile && mat == Material.REDSTONE) return 100;
        int value = switch (mat) {
            case PLAYER_HEAD -> 15000;
            case DRAGON_EGG -> 10000;
            case ENCHANTED_GOLDEN_APPLE -> 7500;
            case NETHER_STAR -> 2500;
            case DEEPSLATE_COAL_ORE -> 2000;
            case NETHERITE_BLOCK -> 6750;
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
                yield 2;
            }
        };

        // Expansion point: External configuration or Soul-specific multipliers
        return value;
    }

    static int calculateTotalPowerCost(SpellNode spell) {
        int totalRequiredPower = 0;
        for (StatementNode stmt : spell.statements) {
            // Base complexity: Range and Amp increase cost exponentially
            int complexity = 1 + stmt.range + (stmt.amplification * 5) + (stmt.elements.size() * 10);
            log("Complexity " + complexity);
            int actionBase = stmt.action.getBaseCost();
            log("Action Base " + actionBase);
            totalRequiredPower += (complexity * actionBase);
            log("Total " + totalRequiredPower);
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
                totalProvided += (getItemValue(item.getType(), false) * item.getAmount());
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

                int val = getItemValue(item.getType(), false);
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
            new BukkitRunnable() {
                @Override
                public void run() {
                    stmt.action.execute(stmt, caster);
                }
            }.runTaskLater(Freedom.get_plugin(), stmt.delay);
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

    public static int castSpell(String text, Location center, Player caster, boolean b) {
        var tokens = tokenize(text);
        var ast = parse(tokens, center,caster);

        var errors = validate(ast);
        if (!errors.isEmpty()) {
            errors.forEach(e -> caster.sendMessage("§c" + e));
            return 0;
        }

        var layers = scanLayers(center);
        int powerRequired = calculateTotalPowerCost(ast);
        boolean isSudo = AdminManager.isSudo(caster);

        if (!isSudo) {
            var costErrors = validateTotalCost(powerRequired, layers);
            if (!costErrors.isEmpty()) {
                costErrors.forEach(e -> caster.sendMessage("§c" + e));
                return 0;
            }
            consumePowerResources(powerRequired, layers);
            execute(ast, caster);
            caster.sendMessage("§aSpell cast successfully! (" + powerRequired + " material consumed)");
        } else {
            execute(ast, caster);
            caster.sendMessage("§aSpell cast successfully! (Sudo mode: No materials consumed)");
        }

        return powerRequired > 0 ? powerRequired : 1;
    }

    public static int castMobileSpell(String text, Player caster, int max) {
        var tokens = tokenize(text);
        var ast = parse(tokens, caster.getLocation(), caster);

        var errors = validate(ast);
        if (!errors.isEmpty()) {
            errors.forEach(e -> caster.sendMessage("§c" + e));
            return 0;
        }

        int powerRequired = calculateTotalPowerCost(ast);
        if (powerRequired > 1000 + max) {
            caster.sendMessage("§cThis spell is too powerful for a mobile focus! (Limit: 1000, Required: " + powerRequired + ")");
            return 0;
        }

        boolean isSudo = AdminManager.isSudo(caster);

        if (!isSudo) {
            if (!hasResources(caster, powerRequired) || powerRequired > getCurrency(caster)) {
                caster.sendMessage("§cInsufficient materials in inventory for this mobile spell!");
                return 0;
            }
            //FIXME fuckass consumeFromInventory doesn't work
            consumeFromInventory(caster, powerRequired);
            execute(ast, caster);
            caster.sendMessage("§aMobile spell cast successfully! (" + powerRequired + " material consumed from inventory)");
        } else {
            execute(ast, caster);
            caster.sendMessage("§aMobile spell cast successfully! (Sudo mode: No materials consumed)");
        }

        return powerRequired > 0 ? powerRequired : 1;
    }

    private static boolean hasResources(Player player, int powerRequired) {
        int totalProvided = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                totalProvided += (getItemValue(item.getType(), true) * item.getAmount());
            }
        }
        return totalProvided >= powerRequired;
    }

    private static void consumeFromInventory(Player player, int powerRequired) {
        int debt = powerRequired;
        ItemStack[] contents = player.getInventory().getContents();

        // First, try to use currency to pay off debt
        int currencyAvailable = getCurrency(player);
        if (debt <= currencyAvailable) {
            // Currency is enough to cover the entire debt
            removeCurrency(debt, player);
            return;
        } else {
            // Use all available currency and reduce debt
            debt -= currencyAvailable;
            setCurrency(0, player);
        }
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            
            // Skip the focus item and the spell book
            if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(FreedomKeys.spellFocus(), org.bukkit.persistence.PersistentDataType.BYTE)) continue;
            if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK) continue;

            int val = getItemValue(item.getType(), true);
            int amountNeeded = (int) Math.ceil((double) debt / val);
            int toTake = Math.min(item.getAmount(), amountNeeded);

            debt -= (toTake * val);
            item.setAmount(item.getAmount() - toTake);
            
            if (debt <= 0) break;
        }
        player.getInventory().setContents(contents);
    }

    public static int cost(String text, Player caster) {
        var tokens = tokenize(text);
        var ast = parse(tokens, caster.getLocation(),caster);

        var errors = validate(ast);
        if (!errors.isEmpty()) {
            errors.forEach(e -> caster.sendMessage("§c" + e));
            return 0;
        }
        int powerRequired = calculateTotalPowerCost(ast);

        if (AdminManager.isSudo(caster)) {
            caster.sendActionBar("§aThis spell is FREE (Sudo mode)!");
        } else {
            caster.sendActionBar("§aThis spell costs (" + powerRequired + ")!");
        }

        return powerRequired;
    }
}