# Constellation Framework

A comprehensive framework for generating, displaying, and managing constellations in Paper/Spigot Minecraft servers. This framework supports predefined constellations, custom star patterns, and procedurally generated random constellations with optional seeding.

## Features

- 🌟 **Predefined Constellations**: 7 named constellations (Orion, Ursa Major, Cassiopeia, Leo, Scorpius, Cygnus, Draco)
- 🎨 **Customizable Appearance**: Control particle types, colors, density, and size
- 📍 **Flexible Positioning**: Place constellations at any location with custom scaling
- ⏱️ **Time Management**: Set how long each constellation stays visible
- ✨ **Despawn Animation**: Smooth fade-out animation when time expires
- 🎲 **Random Generation**: Create unique constellations with procedural generation and optional seeding
- 👥 **Multi-Constellation Support**: Manage multiple active constellations simultaneously
- 🔧 **Builder Pattern**: Easy fluent API for constellation configuration
- 📋 **Command System**: In-game commands for constellation management

## Quick Start

### Basic Usage

```java
// Create a simple predefined constellation
ConstellationData data = new ConstellationData(
    "Orion",
    player.getLocation().add(0, 5, 0),
    ConstellationData.PredefinedConstellation.ORION,
    1.0,  // scale
    200   // time alive in ticks (10 seconds)
);

ConstellationGenerator generator = new ConstellationGenerator(data, plugin);
generator.display();
```

### Using the Builder Pattern (Recommended)

```java
ConstellationData data = new ConstellationGenerator.Builder("Leo", location)
    .withPredefinedConstellation(ConstellationData.PredefinedConstellation.LEO)
    .withScale(1.5)
    .withTimeAlive(300)
    .withDustOptions(new Particle.DustOptions(Color.BLUE, 1.5f))
    .withParticlesPerStar(8)
    .withConnectionLines(true)
    .build();

ConstellationGenerator generator = new ConstellationGenerator(data, plugin);
generator.display();
```

### Using the Manager

```java
ConstellationManager manager = ConstellationManager.getInstance(plugin);

// Display a predefined constellation
manager.displayPredefinedConstellation(
    "orion1",
    "Orion",
    location,
    ConstellationData.PredefinedConstellation.ORION,
    1.0,
    200
);

// Stop it later
manager.stopConstellation("orion1");

// Stop all constellations
manager.stopAllConstellations();
```

## Predefined Constellations

All predefined constellations are available via the `ConstellationData.PredefinedConstellation` enum:

- **ORION**: A hunter constellation with a distinctive belt pattern
- **URSA_MAJOR**: The Great Bear, contains the Big Dipper asterism
- **CASSIOPEIA**: A W-shaped constellation
- **LEO**: The Lion, with a sickle-shaped head
- **SCORPIUS**: The Scorpion, with a curved tail
- **CYGNUS**: The Swan, with wings spread
- **DRACO**: The Dragon, a long winding constellation

## Configuration Options

### ConstellationData Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| name | String | Display name for the constellation |
| centerLocation | Location | Center point where the constellation will be displayed |
| scale | double | Size multiplier for the constellation (1.0 = default) |
| timeAliveInTicks | int | Duration the constellation stays visible (20 ticks = 1 second) |
| particle | Particle | Type of particle to use (default: DUST) |
| dustOptions | DustOptions | Color and size for dust particles |
| starPositions | List<Vector> | Positions of individual stars (relative to center) |
| particlesPerStar | int | Number of particles to spawn per star (default: 5) |
| hasConnectionLines | boolean | Whether to draw lines connecting stars (default: true) |
| seed | long | Seed for random constellation generation |

### Builder Methods

```java
new ConstellationGenerator.Builder(name, location)
    .withPredefinedConstellation(constellation)    // Use a predefined constellation
    .withCustomStars(positions)                     // Use custom star positions
    .withRandomStars(count, seed)                   // Generate random constellation
    .withScale(scale)                               // Set scale multiplier
    .withTimeAlive(ticks)                          // Set display duration
    .withParticle(particle)                        // Set particle type
    .withDustOptions(dustOptions)                  // Set dust particle appearance
    .withParticlesPerStar(count)                   // Set particles per star
    .withConnectionLines(enabled)                  // Enable/disable connection lines
    .build()                                        // Build the constellation data
```

## Random Constellation Generation

### With Specific Seed (Reproducible)

```java
// Same seed = same constellation every time
ConstellationData data = new ConstellationData(
    "Galaxy Alpha",
    location,
    10,      // number of stars
    12345,   // seed
    1.0,     // scale
    200      // time alive
);
```

### Without Seed (Unique Each Time)

```java
// Different constellation every time
ConstellationData data = new ConstellationData(
    "Galaxy Beta",
    location,
    7,
    System.nanoTime(),  // generates unique constellation each time
    1.0,
    200
);
```

## Custom Star Positions

```java
List<Vector> customStars = new ArrayList<>();
customStars.add(new Vector(0, 0, 0));    // Center star
customStars.add(new Vector(2, 1, 0));    // Upper right
customStars.add(new Vector(-1, -2, 0));  // Lower left
// ... add more positions

ConstellationData data = new ConstellationData(
    "My Custom Constellation",
    location,
    customStars,
    1.5,
    300
);
```

## Particle Customization

### Change Particle Color

```java
Particle.DustOptions redOptions = new Particle.DustOptions(Color.RED, 1.5f);
Particle.DustOptions blueOptions = new Particle.DustOptions(Color.BLUE, 2.0f);
Particle.DustOptions customOptions = new Particle.DustOptions(Color.fromBGR(0xFF00FF), 2.0f);

data.setDustOptions(blueOptions);
```

### Use Different Particle Types

```java
data.setParticle(Particle.SPELL_MOB);
// or
data.setParticle(Particle.END_ROD);
// or
data.setParticle(Particle.FIREWORKS_SPARK);
```

## In-Game Commands

### Display a Predefined Constellation

```
/constellation display <name> [scale] [time_in_ticks] [id]
```

Examples:
```
/constellation display ORION
/constellation display LEO 1.5 300 my_leo
```

### Display a Random Constellation

```
/constellation random [star_count] [seed] [scale] [time_in_ticks] [id]
```

Examples:
```
/constellation random
/constellation random 10 12345 1.5 300
/constellation random 7 0 1.0 200 unique_constellation
```

### Stop a Constellation

```
/constellation stop <id>
```

### Stop All Constellations

```
/constellation stopall
```

### List Active Constellations

```
/constellation list
```

### List Predefined Constellations

```
/constellation list-predefined
```

## Despawn Animation

When a constellation reaches its time alive limit, it automatically triggers a despawn animation:
- Duration: 2 seconds (40 ticks)
- Effect: Particles gradually fade out
- This is automatic and cannot be interrupted

## Manager API

The `ConstellationManager` provides a singleton interface for managing multiple constellations:

```java
ConstellationManager manager = ConstellationManager.getInstance(plugin);

// Display constellations
manager.displayPredefinedConstellation(id, name, location, constellation, scale, timeAlive);
manager.displayCustomConstellation(id, name, location, stars, scale, timeAlive);
manager.displayRandomConstellation(id, name, location, starCount, seed, scale, timeAlive);
manager.displayConstellation(id, builder);

// Manage constellations
manager.stopConstellation(id);
manager.stopAllConstellations();
manager.updateConstellationLocation(id, newLocation);

// Query constellations
manager.isActive(id);
manager.getConstellation(id);
manager.getActiveCount();
manager.getActiveConstellationIds();

// Utilities
manager.getPredefinedConstellationNames();
manager.getPredefinedConstellation(name);
```

## Advanced Examples

### Multi-Color Layered Constellation

```java
Location center = player.getLocation().add(0, 5, 0);

// Red outer layer
ConstellationData red = new ConstellationGenerator.Builder("Layer Red", center)
    .withPredefinedConstellation(ConstellationData.PredefinedConstellation.DRACO)
    .withScale(3.0)
    .withDustOptions(new Particle.DustOptions(Color.RED, 1.0f))
    .withParticlesPerStar(3)
    .build();

// Yellow middle layer
ConstellationData yellow = new ConstellationGenerator.Builder("Layer Yellow", center)
    .withPredefinedConstellation(ConstellationData.PredefinedConstellation.DRACO)
    .withScale(2.0)
    .withDustOptions(new Particle.DustOptions(Color.YELLOW, 2.0f))
    .withParticlesPerStar(5)
    .build();

// White center layer
ConstellationData white = new ConstellationGenerator.Builder("Layer White", center)
    .withPredefinedConstellation(ConstellationData.PredefinedConstellation.DRACO)
    .withScale(1.0)
    .withDustOptions(new Particle.DustOptions(Color.WHITE, 2.5f))
    .withParticlesPerStar(8)
    .build();

new ConstellationGenerator(red, plugin).display();
new ConstellationGenerator(yellow, plugin).display();
new ConstellationGenerator(white, plugin).display();
```

### Creating Unique Procedural Constellations

```java
// Create a unique constellation using current system time as seed
ConstellationData data = new ConstellationGenerator.Builder("Unique Galaxy", location)
    .withRandomStars(15, System.currentTimeMillis())
    .withScale(2.0)
    .withTimeAlive(500)
    .withDustOptions(new Particle.DustOptions(Color.GREEN, 1.8f))
    .build();
```

### Tracking Active Constellations

```java
ConstellationManager manager = ConstellationManager.getInstance(plugin);

// Get all active constellation IDs
for (String id : manager.getActiveConstellationIds()) {
    ConstellationGenerator gen = manager.getConstellation(id);
    if (gen != null) {
        if (gen.isDespawning()) {
            player.sendMessage("Constellation " + id + " is despawning");
        } else {
            player.sendMessage("Constellation " + id + " is active");
        }
    }
}
```

## Integration with Plugin.yml

To enable the commands, add the following to your `plugin.yml`:

```yaml
commands:
  constellation:
    description: "Manage constellations"
    usage: "/constellation <subcommand>"
    permission: "freedom.constellation"
permissions:
  freedom.constellation:
    description: "Allows the use of constellation commands"
    default: op
```

## File Structure

```
Constellations/
├── ConstellationData.java       # Data class for constellation configuration
├── ConstellationGenerator.java  # Main framework for displaying constellations
├── ConstellationManager.java    # Singleton manager for multiple constellations
├── ConstellationCommand.java    # Command executor
├── ConstellationExamples.java   # Usage examples and demonstrations
└── README.md                    # This file
```

## Performance Considerations

- **Particle Count**: Each star spawns `particlesPerStar` particles per tick. Adjust this based on server performance.
- **Star Count**: More stars = more particles = higher performance impact.
- **Active Constellations**: The manager can handle multiple constellations, but consider server load.
- **Connection Lines**: Drawing lines increases particle count. Disable if needed for performance.

## Tips & Best Practices

1. **Use Reasonable Scales**: Scales above 5.0 may be visually difficult to see clearly
2. **Adjust Particle Density**: Use `withParticlesPerStar()` to balance visuals and performance
3. **Seeding for Reproducibility**: Use specific seeds if you want consistent constellations
4. **Color Selection**: Test colors with different time of day and weather for best visibility
5. **Time Duration**: Use multiples of 20 for even second durations (e.g., 20 = 1 sec, 200 = 10 sec)

## Future Enhancement Ideas

- Animated constellations (orbiting stars, rotating, etc.)
- Constellations that follow players
- Sound effects synchronized with display
- Custom constellation templates stored in configuration files
- Constellation spawning at random locations
- Interactive constellation selection menu

---

**Framework Version**: 1.0  
**Minecraft Version**: 1.21+  
**Server Type**: Paper/Spigot

