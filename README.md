# MCA x MineColonies: Full Integration

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/ModLoader-NeoForge-orange.svg)](https://neoforged.net/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A deep, seamless integration mod for **Minecraft 1.21.1 (NeoForge)** that bridges **MineColonies** and **MCA Reborn (Minecraft Comes Alive)**. It replaces blocky citizen models with humanized MCA 3D models and textures, integrates MCA growth and age progression stages, enables item-based customization, provides a top-right talk button in MineColonies GUI to open an interactive MCA dialogue screen, and overrides colony child births with MCA genetics.

---

## 🌟 Features

### 1. 👶 Age Progression & Natural Growth (Baby to Adult)
- **5 Distinct Life Stages**:
  - **Baby** (0–2 min): Crawling / small swaddle model with baby clothing.
  - **Toddler** (2–5 min): Early growth stage with toddler clothing and cute proportions.
  - **Child** (5–10 min): Active child stage with child clothing.
  - **Teen** (10–15 min): Youth stage preparing for adulthood.
  - **Adult** (15+ min): Full adult model with profession-specific work clothing.
- **Natural Transition**: When a child citizen grows into an Adult, they automatically graduate into a full MineColonies adult citizen, ready for worker jobs and assignments!
- **Growth Acceleration**: Feed children with Golden Apples, Cake, Apples, or Cookies to accelerate their growth into adulthood.

### 2. 👗 Gender Consistency & Human Models
- **Female Citizens**: Alex slim 3-pixel arm geometry, female face, female hairstyle, and female profession outfits.
- **Male Citizens**: Steve standard 4-pixel arm geometry, male face, male hairstyle, and male profession outfits.
- **Persistent Genetics**: Appearance is deterministically seeded from citizen UUID and synchronized across chunk loads and world saves.

### 3. ✂️ Interactive Customization with MCA Items
Right-click any citizen with MCA items or vanilla tools to customize them in real-time:
- **Dye Items**: Applies dye to the citizen's hair (Black, Brown, Blonde, Ginger/Red, Platinum, Pink, Cyan, Blue, etc.)!
- **Comb / Shears**: Cycles through 40 unique hairstyles with sound effects.
- **Needle & Thread**: Cycles through clothing outfits and variants.
- **Potion of Femininity**: Transforms citizen into Female (Alex model, female textures, female voice pitch).
- **Potion of Masculinity**: Transforms citizen into Male (Steve model, male textures, male voice pitch).
- **Villager Editor / Dressing Mirror**: Opens the full 3D visual customization GUI.

### 4. 💬 MCA Dialogue Screen (Top-Right Button in MineColonies GUI)
- Whenever you open a citizen's window in MineColonies (`MainWindowCitizen`), a **💬 Talk Icon Button** appears in the top-right corner.
- Clicking the button opens the **MCA Dialogue Screen**:
  - Live 3D animated citizen avatar tracking your cursor.
  - Affection / Hearts meter.
  - Interactive options: **Chat**, **Tell Joke**, **Story**, **Flirt**, **Compliment**, and **Customize Look**.
  - Citizen responds with unique personality-based dialogue lines, villager sounds, and particle reactions!
  - Button to smoothly transition back to the MineColonies colony window.

### 5. 🍼 Colony Child Birth Override
- When citizen reproduction occurs in MineColonies houses, the newborn baby is initialized with MCA genetics (inheriting hair color and skin tone from colony parents).
- Plays celebration sounds and particle effects upon birth.

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
   - `minecolonies-1.21.1-*.jar` (and its required dependencies: `structurize`, `blockui`, `multipiston`, `domum_ornamentum`)
   - `minecraft-comes-alive-reborn-1.21.1-*.jar`
3. Launch the game. Colony citizens will immediately feature MCA human models, growth stages, customization tools, and dialogue options!

---

## 🛠️ Building From Source

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
