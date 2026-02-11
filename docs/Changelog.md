# Changelog

All notable changes to RelishTravel.

## [1.0.1] - 2026-02-11

### Fixed

**Achievement System:**
- Fixed Elytra advancement detection for modern vanilla key `end/elytra` (kept legacy fallbacks).
- Added criterion-level cancellation using Paper's `PlayerAdvancementCriterionGrantEvent` to better block vanilla Elytra advancement when using virtual RelishTravel Elytra.
- Kept fallback advancement criteria revocation for compatibility.
- Added duplicate-message protection so custom achievement announcements are not broadcast twice.

**Custom Achievement Message:**
- Removed forced player name color to match vanilla style.
- Replaced plain chat description line with hover text on `[Sky Traveler]`.
- Switched custom announcement to Adventure components for proper hover support.

## [1.0.0] - 2026-02-09

### Initial Release

**Features:**
- ⚡ Charge-based launch system
- 🚀 Mid-air boost mechanics
- 🛡️ Safety features and damage prevention
- 🎨 Visual effects (particles, sounds, action bar)
- 🌍 Multi-language support (EN, AR)
- 🔧 Extensive configuration options
- 🔑 Permission-based boost limits
- 🛠️ Virtual Elytra support

**Technical:**
- Minecraft 1.20+
- Paper/Purpur support
- Java 17+
- Standalone (no dependencies)

---

## Upcoming Features

### Planned for 1.1.0
- PlaceholderAPI integration
- Statistics tracking
- More languages
---

## Support

- **Discord**: [Join server](https://discord.gg/jDr2KZcGXk)
- **GitHub**: [View source](https://github.com/iM5LB/relishtravel)
- **Issues**: [Report bugs](https://github.com/iM5LB/relishtravel/issues)
