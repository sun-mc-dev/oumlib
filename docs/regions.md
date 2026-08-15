# Regions

`dev.oum.oumlib.math.region` · Paper

---

## Region Types

Regions define 3D areas in a world. You can check if a location is inside, get all blocks, and track enter/leave events.

### Cuboid

```java
CuboidRegion region = CuboidRegion.of("world", pos1, pos2);
// pos1 and pos2 are Location or Vector3D corners
```

### Sphere

```java
SphereRegion region = SphereRegion.of("world", center, radius);
```

### Cylinder

```java
CylinderRegion region = CylinderRegion.of("world", center, radius, minY, maxY);
```

### Polygon

```java
List<Vector2D> points = List.of(
    Vector2D.of(0, 0),
    Vector2D.of(10, 0),
    Vector2D.of(10, 10),
    Vector2D.of(0, 10)
);
PolygonRegion region = PolygonRegion.of("world", points, 60, 120);
// minY=60, maxY=120
```

### Compound

Combine multiple regions:

```java
CompoundRegion region = CompoundRegion.of(region1, region2, region3);
```

---

## Checking Locations

```java
boolean inside = region.contains(location);
boolean inside = region.contains(x, y, z);
```

---

## Getting Blocks / Chunks

```java
List<Block> blocks = region.getBlocks();
Set<Chunk> chunks = region.getChunks();
```

---

## Region Bounds

```java
Location min = region.getMin();
Location max = region.getMax();
Location center = region.getCenter();
double volume = region.getVolume();
```

---

## Serialization

Regions can be serialized to and from maps for storage:

```java
Map<String, Object> data = region.serialize();

// later
CuboidRegion region = CuboidRegion.deserialize(data);
```

---

## Region Tracker

Track players entering and leaving regions:

```java
OumLib.regions().register("spawn-area", region);

OumLib.regions().onEnter("spawn-area", player -> {
    Text.send(player, "<green>Welcome to spawn!");
});

OumLib.regions().onLeave("spawn-area", player -> {
    Text.send(player, "<yellow>Leaving spawn area");
});
```

The tracker checks player positions every tick and fires events when they cross region boundaries.

### Unregister

```java
OumLib.regions().unregister("spawn-area");
```

All regions are cleaned up on `OumLib.shutdown()`.
