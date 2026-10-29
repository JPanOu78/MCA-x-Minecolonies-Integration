# MCA x MineColonies: Citizen Reskin

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/ModLoader-NeoForge-orange.svg)](https://neoforged.net/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A seamless client-side visual integration mod for **Minecraft 1.21.1 (NeoForge)** that replaces all **MineColonies** citizen models with humanized models, skins, hairstyles, and facial features from **Minecraft Comes Alive (MCA) Reborn** — while preserving 100% of MineColonies' backend worker logic, AI, jobs, and civilization mechanics.

---

## 🌟 What This Mod Does

MineColonies features an extensive colony management system, but its citizens retain traditional or default blocky villager-style aesthetics. This mod functions as a **pure visual override ("cosmetic shell")**:

- 👤 **Automatic Model Replacement**: Automatically renders MineColonies citizens using standard player-like humanoid geometry without requiring any commands, configuration, or manual switching.
- 👗 **Gender-Specific Geometry**:
  - **Female Citizens**: Uses the slim 3-pixel arm (Alex) model with MCA female hair, face, and base skin textures.
  - **Male Citizens**: Uses the standard 4-pixel arm (Steve) model with MCA male hair, face, and base skin textures.
- 🎨 **Multi-Layer Rendering Pipeline**:
  1. **Base Skin Layer**: Varied skin tones derived from MCA assets.
  2. **Face & Expression Layer**: Dynamic expressions, eyes, and facial details.
  3. **Hairstyle Layer**: Diverse hairstyles rendered over the head mesh.
  4. **Job & Profession Clothing**: Automatically maps MineColonies worker assignments (Farmers, Miners, Guards, Lumberjacks, Cooks, Smiths, Couriers, Clerics, etc.) to authentic MCA clothing overlays.
- 🔒 **Deterministic Appearance**: Every citizen's appearance (skin tone, hair index, facial expression) is mathematically seeded by their unique Entity UUID. Their look remains completely persistent across chunk reloads, world saves, and game restarts.
- ⚡ **Zero Gameplay Friction**: All MineColonies AI schedules, pathfinding, building assignments, resource requests, happiness, and level progression run completely untouched.

---

## 📋 Requirements

| Mod | Version | Platform |
|---|---|---|
| **Minecraft** | `1.21.1` | Java Edition |
| **NeoForge** | `21.1.x`+ | Mod Loader |
| **MineColonies** | `1.21.1` (with Structurize, BlockUI, Multi-Piston, Domum Ornamentum) | Colony Framework |
| **MCA Reborn** | `1.21.1` | Asset & Model Provider |

---

## 🚀 Installation

1. Make sure **Minecraft 1.21.1** is installed with **NeoForge**.
2. Download and place the following mods into your `.minecraft/mods` folder:
   - `mcaminecolonies-1.0.0.jar`
   - `minecolonies-1.21.1-*.jar` (along with its dependencies: `structurize`, `blockui`, `multipiston`, `domum_ornamentum`)
   - `minecraft-comes-alive-reborn-1.21.1-*.jar`
3. Launch the game. Any active or newly spawned colony citizens will instantly display the MCA human appearance.

---

## 🛠️ Building From Source

If you want to compile the project yourself:

### Prerequisites
- **Java 21 JDK** (e.g., Microsoft OpenJDK 21 or Eclipse Temurin 21)
- Git

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/mrzero784/MCA-x-Minecolonies-Integration.git
cd MCA-x-Minecolonies-Integration

# Build using Gradle Wrapper
./gradlew build
```

The compiled mod JAR will be located in:
```
build/libs/mcaminecolonies-1.0.0.jar
```

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)** to maintain compatibility with both MineColonies and MCA Reborn open-source licenses.
