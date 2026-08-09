# Effects

`dev.oum.oumlib.effect` · Paper

---

## Particles

Play particles with a fluent API:

```java
Effects.particle(Particle.FLAME)
    .count(20)
    .offset(0.5, 0.5, 0.5)
    .speed(0.1)
    .spawn(location);
```

Spawn at a player's location:

```java
Effects.particle(Particle.HEART)
    .count(5)
    .offset(0.3, 0.5, 0.3)
    .spawn(player.getLocation());
```

---

## Sounds

```java
Effects.sound(Sound.ENTITY_PLAYER_LEVELUP)
    .volume(1.0f)
    .pitch(1.2f)
    .play(player);
```

Play at a location (everyone nearby hears it):

```java
Effects.sound(Sound.BLOCK_NOTE_BLOCK_PLING)
    .volume(0.8f)
    .pitch(2.0f)
    .play(location);
```

---

## Particle Shapes

### Line

Draw a line of particles between two points:

```java
Effects.line(start, end, Particle.FLAME, 20); // 20 points along the line
```

### Circle

```java
Effects.circle(center, radius, Particle.ENCHANT, 30); // 30 points around the circle
```

### Helix

```java
Effects.helix(center, radius, height, Particle.FLAME, rotations, points);
```

### Bezier Curve

```java
Effects.bezier(start, control, end, Particle.FLAME, 30);
```

---

## Combining

Spawn multiple effects at once:

```java
Location loc = player.getLocation();

Effects.sound(Sound.ENTITY_PLAYER_LEVELUP).volume(1f).pitch(1.2f).play(player);
Effects.particle(Particle.HAPPY_VILLAGER).count(15).offset(0.5, 0.5, 0.5).spawn(loc);
Effects.particle(Particle.FIREWORK).count(5).offset(0.2, 0.2, 0.2).speed(0.05).spawn(loc);
```
