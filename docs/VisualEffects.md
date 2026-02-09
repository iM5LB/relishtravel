# Visual Effects

Customizable particles, sounds, and displays.

## Particles

```yaml
effects:
  particles: true
  particle-type: "ELECTRIC_SPARK"
  particle-count: 5
  particle-radius-min: 0.5
  particle-radius-max: 1.0
```

Electric sparks that grow as you charge.

## Sounds

```yaml
effects:
  sounds: true
  sound-type: "BLOCK_BEACON_ACTIVATE"
  sound-volume: 0.5
  sound-pitch-min: 0.5
  sound-pitch-max: 2.0
```

Dynamic pitch that increases with charge level.

## Action Bar Displays

```yaml
effects:
  speed-display: true
  boost-display: true
  action-bar-update-ticks: 4
```

- **Charge Progress** - Shows percentage and bar
- **Speed Display** - Real-time speed during flight
- **Boost Counter** - Remaining boosts shown
