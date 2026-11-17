# Changelog - MCA x Minecolonies

## [1.0.4] - Full MCA Parity, Spasm Fix & Appearance Persistence

### Added
- **Full MCA Reborn Field & Genetic Parity:**
  - Integrated complete genetic and visual field parity with original MCA Reborn into `CitizenMCAData`, `CitizenAppearance`, and entity mixins.
  - **Height & Body Proportions:** Added `size`, `width`, and `breast` genetic scaling (`Genetics.SIZE`, `Genetics.WIDTH`, `Genetics.BREAST`), dynamically scaling citizen models in `CitizenMCARenderer` with safe limits.
  - **Heterochromia & Eye Customization:** Added dual-eye coloring (`eyeColor`, `eyeColorLeft`) and `eyeBrightness` genetics (`Genetics.EYE_BRIGHTNESS`), enabling distinct left/right eye colors and dye persistence.
  - **Layered Hairstyle Elements:** Added individual layer support for `hairBase`, `hairBangs`, `hairBack`, `hairFront`, and `hairExtra`.
  - **MCA Traits System:** Synchronized citizen trait sets (`traits`) between entity data, NBT storage, and MineColonies colony files (`colony_X.dat`).
  - **Mood & Armor Visibility:** Added `mood` and `armorWear` fields, enabling MCA armor toggle and emotional state synchronization.
  - **Clothing Lock Feature:** Shift-Right-Clicking a citizen with a Needle & Thread (`mca:needle_and_thread`) toggles casual clothing lock (`clothingLocked`), preventing MineColonies from forcing job uniforms over custom outfits.
  - **Parent Stat & Trait Inheritance on Colony Birth:**
    - Newly born colony children now inherit skills from their parents based on parental proficiency (inheriting up to +5 bonus levels proportional to parents' MineColonies skill levels).
    - MCA traits are passed down to children with high probability.
    - Added trait synergies: `Athletic` (+2 Athletics, +1 Agility, +1 Stamina, +10% move speed), `Tough` (+2 Strength, +1 Stamina), and `Weak` (-1 Strength, -8% move speed).
  - **Automatic MCA Baby Birth & Colony Adoption (`ColonyAdoptionHelper` & `BabyItemMixin`):**
    - When an MCA infant/child is birthed (`BabyItem.birthChild`) or spawns within a colony's territory, it is automatically converted and enrolled into MineColonies as a full colony citizen (`AbstractEntityCitizen`).
    - Copies all facial, skin tone, hair layers, eye colors, body dimensions, and MCA traits into the new citizen.
    - Automatically links the child to both parents in MineColonies (`cData.setParents`, spouse `addChildren`) and updates MCA's `FamilyTree`.
    - Added Shift-Right Click adoption allowing colony leaders to manually adopt any existing MCA child villager into their colony.

### Fixed
- **Hair Color & Hairstyle Mutating on World Reload:**
  - Resolved an issue where citizen hairstyles and hair colors randomized every time the player exited and re-entered the world.
  - Added full `HairStyle` persistence and serialization to `CitizenMCAData`, synchronizing MCA's layered hair styles directly into MineColonies colony data (`colony_X.dat`) and entity data packets.
  - Implemented deterministic fallback hairstyle resolution (`resolveHairStyle`) mapped from persistent seeds so legacy citizens without explicit hairstyles retain consistent styles across game sessions.
  - Fixed hair color resolution in `CitizenAppearance` so stored natural and dyed hair colors are never bypassed in favor of newly randomized genetics.
  - Enhanced Comb & Shears interaction to cycle through modern MCA layered hairstyles in addition to classic hair variants.
- **Citizen Arm & Torso Spasming / Jittering:**
  - Resolved an issue where non-guard citizens (such as Builders, Crafters, or citizens waiting at their hut blocks with active work requests) suffered from violent, continuous arm and torso twitches/spasms.
  - Removed artificial sine wave oscillation previously injected into `model.attackTime` whenever MineColonies synced `"working"` render metadata.
  - Restored authentic Minecraft swing animations driven directly by entity actions (`entity.getAttackAnim(partialTicks)`), allowing smooth swings when workers physically interact or swing tools.
- **Client Rendering Performance:**
  - Removed redundant per-frame guard detection string checks and held item inspections during the client render pass.

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
