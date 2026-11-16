# Changelog - MCA x Minecolonies

## [1.0.3] - Citizen Face & Appearance Persistence Fix

### Fixed
- **Citizen Face Mutating on Game Reload:**
  - Fixed a critical calculation bug where `Genetics.FACE` was divided by `22.0F` instead of `11.0F` during initialization, causing the citizen's face index to be halved on every game save/load cycle until collapsing to face 0.
  - Corrected face index clamping in `CitizenMCAData` from modulo 22 to modulo 11, matching MCA's 11 face variants (`0.png` through `10.png`).
- **Persistent Colony Storage (`CitizenDataMixin`):**
  - Created `CitizenDataMixin` implementing `IMCACitizenData` to save and load `MCAData` directly inside MineColonies colony data (`colony_X.dat`).
  - Citizen visual traits (face, skin, hair, clothes, genetics) are now permanently retained across entity despawns, recalls, player teleports, and server restarts.
- **Save/Load NBT Tag Synchronization:**
  - Unified `MCAData` and `MCAColoniesData` tag serialization and deserialization across `AbstractEntityCitizenMixin` and `EntityCitizenMixin`.
  - Restored saved appearance data prior to calling `initMCA()`, preventing newly loaded entities from regenerating randomized default traits.
- **Genetics & Skin Tone Consistency:**
  - Synchronized `Genetics.MELANIN` and `Genetics.HEMOGLOBIN` from `CitizenMCAData` to prevent random skin tone shifts on game restart.
  - Made fallback appearance generation deterministic using `colonyId` and `citizenId` seeds.

## [1.0.2] - Cross-Mod Job Appearance & Compatibility Update

### Added
- **Mapping for Addon Professions:** Extended `CitizenAppearance.mapMinecoloniesJobToMCA` to recognize jobs added by MineColonies addons:
  - `orchardist` (from `MineColonies_Compatibility`) -> Mapped to MCA 3D Farmer clothing (`"farmer"`).
  - `butcher` (from `MineColonies_Compatibility`) -> Mapped to MCA 3D Butcher clothing (`"butcher"`).
  - `fluid_manager` (from `MineColonies_Compatibility`) -> Mapped to MCA 3D Toolsmith clothing (`"toolsmith"`).
  - `gunner` / `irons_gunner` -> Mapped to MCA 3D Archer / Guard clothing (`"archer"`).
  - `mage` / `irons_mage` -> Mapped to MCA 3D Cleric / Wizard clothing (`"cleric"`).

### Changed
- Improved clothing texture fallback resolution so unmapped addon jobs smoothly resolve to default villager apparel rather than missing texture cubes.

### Compatibility & Standalone Safety
- **Soft Dependency Checks:**
  - Citizen rendering, interactions, dialogue screens, and growth mechanics exclusively hook via duck typing (`VillagerLike`, `CompassionateEntity`) and safe mixins onto `AbstractEntityCitizen`.
  - Fully compatible with `minecolonies`, `mca` (MCA Reborn), `minecolonies_compatibility`, and `gourmetcraft`.
  - Automatically adapts citizen model animations and clothing when external job entries are registered by third-party mods.
