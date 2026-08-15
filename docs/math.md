# Math

`dev.oum.oumlib.math` · Paper / Velocity

---

## Vector2D

Immutable 2D vector (x, z) — useful for flat-plane calculations like polygon regions, map coordinates, or distance checks ignoring Y.

```java
Vector2D a = Vector2D.of(10, 20);
Vector2D b = Vector2D.of(30, 40);

Vector2D sum = a.add(b);           // (40, 60)
Vector2D diff = a.subtract(b);     // (-20, -20)
Vector2D scaled = a.multiply(2);   // (20, 40)
double dist = a.distance(b);       // distance between two points
double dot = a.dot(b);
Vector2D norm = a.normalize();
Vector2D mid = a.lerp(b, 0.5);     // midpoint
```

Convert to/from Bukkit types:

```java
Vector2D from = Vector2D.fromLocation(location);
Location loc = from.toLocation(world, 64.0); // add Y
```

---

## Vector3D

Immutable 3D vector. Same idea as Bukkit's `Vector` but immutable and with more math.

```java
Vector3D a = Vector3D.fromLocation(location);
Vector3D b = Vector3D.fromEntity(entity);

Vector3D sum = a.add(b);
Vector3D cross = a.cross(b);
double dot = a.dot(b);
double dist = a.distance(b);
Vector3D norm = a.normalize();
Vector3D lerped = a.lerp(b, 0.5);
Vector3D rotated = a.rotateY(Math.toRadians(45));
```

Convert back:

```java
Vector bukkit = v.toBukkitVector();
Location loc = v.toLocation(world);
```

Binary I/O:

```java
v.write(dataOutputStream);
Vector3D v = Vector3D.read(dataInputStream);
```

---

## Volume3D

Axis-aligned bounding box defined by two corners:

```java
Volume3D box = Volume3D.of(min, max);

boolean inside = box.contains(point);
boolean overlaps = box.intersects(otherBox);
Volume3D expanded = box.expand(2.0);
Vector3D center = box.center();
```

---

## FastMath

Common math operations without the overhead of `Math.`:

```java
FastMath.clamp(value, min, max);
FastMath.lerp(a, b, t);
FastMath.floor(double);
FastMath.ceil(double);
FastMath.round(double, decimals);
FastMath.sq(x);                // x * x
FastMath.distanceSquared(x1, y1, z1, x2, y2, z2);
```

---

## Noise

Perlin and simplex noise for terrain generation, particle effects, or anything that needs smooth randomness:

```java
double val = Noise.perlin2D(x, z, seed, frequency);
double val = Noise.simplex2D(x, z, seed);
double val = Noise.perlin3D(x, y, z, seed, frequency);

// Octave noise for more detail
double val = Noise.fractal2D(x, z, seed, octaves, frequency, lacunarity, persistence);
```

---

## Easing

Easing functions for animations:

```java
double t = Easing.easeInOutCubic(progress);  // 0.0 to 1.0
double t = Easing.easeOutBounce(progress);
double t = Easing.easeInElastic(progress);
```

Available: `linear`, `easeInQuad`, `easeOutQuad`, `easeInOutQuad`, `easeInCubic`, `easeOutCubic`, `easeInOutCubic`, `easeInBack`, `easeOutBack`, `easeInOutBack`, `easeInElastic`, `easeOutElastic`, `easeInBounce`, `easeOutBounce`, `easeInOutBounce`.

---

## MathEval

Evaluate math expressions from strings:

```java
double result = MathEval.evaluate("2 + 3 * 4");         // 14.0
double result = MathEval.evaluate("sin(pi / 2)");       // 1.0
double result = MathEval.evaluate("sqrt(144)");          // 12.0
```

Supports: `+`, `-`, `*`, `/`, `^`, `%`, parentheses, `sin`, `cos`, `tan`, `sqrt`, `abs`, `floor`, `ceil`, `round`, `min`, `max`, `pi`, `e`.

---

## Geometry3D

3D geometry helpers:

```java
Geometry3D.rotateAroundY(point, origin, angle);
Geometry3D.rotateAroundX(point, origin, angle);
Geometry3D.closestPointOnLine(lineStart, lineEnd, point);
Geometry3D.distanceToLine(lineStart, lineEnd, point);
```

---

## Locations

Location serialization and utilities:

```java
String serialized = Locations.serialize(location);       // "world,10.5,64.0,20.3,90.0,0.0"
Location loc = Locations.deserialize(serialized);

Locations.center(location);   // center of the block
Locations.blockLocation(loc); // snap to block coords
```

---

## Chance

Random chance checks:

```java
if (Chance.percent(25)) {
    // 25% chance to run
}

if (Chance.oneIn(10)) {
    // 1 in 10 chance
}
```

---

## Matrix3

3x3 rotation matrix:

```java
Matrix3 rot = Matrix3.rotationY(Math.toRadians(45));
Vector3D rotated = rot.multiply(vector);
```

---

## Quaternion

Quaternion rotations:

```java
Quaternion q = Quaternion.fromAxisAngle(0, 1, 0, Math.toRadians(90));
Vector3D rotated = q.rotate(vector);
Quaternion combined = q1.multiply(q2);
```

---

## WeightedSelector

Pick random items with weights:

```java
WeightedSelector<String> selector = WeightedSelector.<String>create()
    .add("common", 60)
    .add("rare", 30)
    .add("legendary", 10);

String picked = selector.select(); // weighted random pick
```
