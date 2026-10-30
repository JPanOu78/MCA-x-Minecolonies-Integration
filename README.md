# MCA x MineColonies: Full Integration

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/ModLoader-NeoForge-orange.svg)](https://neoforged.net/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A deep, seamless integration mod for **Minecraft 1.21.1 (NeoForge)** that completely unifies **MineColonies** and **MCA Reborn (Minecraft Comes Alive)**. 

Every MineColonies citizen is rendered as an MCA Reborn human with 3D models, layered hair, customizable genetics, age stages, full dialogue trees, and balanced gifting mechanics — all while preserving 100% of MineColonies worker AI, pathfinding, and colony management systems.

---

## 🌟 Key Features

### 1. 👤 Complete MCA Humanoid Rendering & Appearance
- **Native 3D Player-Like Models**: Replaces standard blocky villagers with MCA's custom humanoid models:
  - **Female Citizens**: Alex slim 3-pixel arm geometry, female facial textures, feminine hairstyles, and female profession outfits.
  - **Male Citizens**: Steve standard 4-pixel arm geometry, male facial textures, masculine hairstyles, and male profession outfits.
- **Layered 3D Hair**: Citizens feature multi-layered hair (Hair Base, Bangs, Back, Front, Extra) with individual RGB dye coloring.
- **Heterochromia / Dual Eye Colors**: True left and right eye color genetics.
- **Realistic Melanin & Hemoglobin Skin Tones**: Skin palettes mapped directly to MCA genetics for accurate, non-inverted skin rendering.
- **Profession & Wardrobe Compatibility**: Citizens wear profession-specific outfits that adapt automatically to their gender and age.

### 2. 👶 Age Progression & Life Stages (Baby to Adult)
- **5 Distinct Life Stages**:
  - **Baby** (0–2 min): Small swaddle model with crawl animations.
  - **Toddler** (2–5 min): Early childhood stage with cute proportions.
  - **Child** (5–10 min): Active child model with child outfits.
  - **Teen** (10–15 min): Youth stage preparing for career assignments.
  - **Adult** (15+ min): Full adult model capable of taking on any colony profession.
- **Proportional Hitboxes**: Hitbox and eye height dynamically adjust according to MCA's `VillagerDimensions`.
- **Growth Acceleration**: Feed children Cake, Golden Apples, Apples, or Cookies to speed up their growth into adulthood.
- **Colony Graduation**: When a child grows into an adult, they seamlessly become a full MineColonies worker citizen.

### 3. 💬 MCA Dialogue System & Fatigue Mechanics
- **Two-Way GUI Navigation**:
  - **💬 Talk Button**: Open any citizen's MineColonies window (`MainWindowCitizen`) and click the **💬 Talk Button** in the top-right corner to open MCA's native `InteractScreen`.
  - **📊 Stats Button**: While interacting in MCA's `InteractScreen`, click the **📊 Stats** button in the top-right corner to instantly open MineColonies' `MainWindowCitizen` and view all 11 skill stats, health, saturation, happiness, and work assignments.
- **Multi-Turn Interactive Dialogue**:
  - Full conversational topics: **Chat**, **Tell Joke**, **Story**, **Flirt**, **Kiss**, **Hug**, **Play** (Rock-Paper-Scissors for kids), **Apologize**, **Rumors**, and **Hire**.
  - Citizen responses reflect their unique traits, mood, and personality.
- **Fatigue & Conversation Limits**:
  - Citizens have interaction fatigue (`data.getFatigue()`). Engaging in conversations builds fatigue.
  - If a citizen is exhausted (`Fatigue >= 4`), they will politely decline further conversation with angry particles and sounds until they rest.
  - Fatigue gradually decays over time per MCA's interaction cooldown.
- **Polished Dialogue UI**:
  - Citizen replies (jokes, stories, chat lines) appear directly inside the speech bubble in the GUI.
  - **Click-to-Dismiss**: Once the citizen delivers their final line, simply left-click anywhere on screen to close the speech bubble and return to the main interaction menu without needing to press `ESC`.
  - Double-click protection prevents accidental choice spamming.

### 4. 🎁 MCA Gifting System & Saturation Rules
- **Balanced Gifting Mechanics**: Completely replaces unrestricted gifting with MCA's authentic preferences and satisfaction formulas:
  - **Dynamic Item Scoring**: Tools and weapons are scored by damage and speed; projectiles by range; armor by defense/toughness; food by nutrition.
  - **Trait & Personality Satisfaction**: Citizens evaluate gifts based on their unique personality, sending an `AnalysisResults` breakdown to the player.
  - **Gift Saturation (Diminishing Returns)**: Repeatedly giving the same item results in severe satisfaction penalties (`desaturation = count * factor * base^exponent`).
  - **Saturated Rejection**: If an item is spammed too frequently, the citizen refuses with `"gift.saturated"` and will not consume the item.
  - **Disliked Gifts**: Unfavorable items are rejected (`gift.fail`) without being wasted.
  - **Accepted Gifts**: Accepted items consume 1 item from hand, insert it into `InventoryCitizen`, reward hearts/mood, and play heart celebration particles.
  - **Saturation Decay**: Oldest gifted items in the saturation queue decay every 1 in-game day (24,000 ticks).

### 5. 💍 Relationships, Marriage & Newborn Genetics
- **Relationship Items**: Full support for MCA relationship items:
  - **Wedding Ring / Engagement Ring**: Propose and marry citizens.
  - **Bouquet**: Express affection and boost romance.
  - **Divorce Papers**: End marital relationships cleanly.
- **Inherited Genetics**: Children born from colony reproduction inherit hair colors, eye colors, and skin tones from their colony parents.

### 6. ✂️ In-Game Customization & Villager Editor
- **Villager Editor & Dressing Mirror**: Open the full 3D visual editor on any citizen to customize age, hair styles, hair dyes, skin colors, and gender in real-time.
- **Real-Time Item Customization**:
  - **Comb / Shears**: Cycle through hairstyles with clicking sounds.
  - **Needle & Thread**: Cycle through clothing styles.
  - **Dye Items**: Dye citizen hair directly in-world.
  - **Potion of Femininity / Masculinity**: Toggle gender between female and male.

### 7. 🏰 Flawless MineColonies Worker Compatibility
- **100% Colony AI**: Citizens execute all MineColonies jobs (Builders, Miners, Farmers, Guards, Knights, Cooks, etc.) without interference.
- **Tavern Visitor Protection**: Tavern visitors and wandering recruits remain strictly on their MineColonies Recruit GUI, preventing accidental citizen window overrides.

---

## 📋 Requirements

| Dependency | Required Version | Note |
|---|---|---|
| **Minecraft** | `1.21.1` | Java Edition |
| **NeoForge** | `21.1.x`+ | Mod Loader |
| **MineColonies** | `1.21.1` | Colony Management Mod |
| **MCA Reborn** | `7.7.36-beta.3`+ | Minecraft Comes Alive Reborn |
| **Structurize** | `1.21.1` | Required by MineColonies |
| **BlockUI** | `1.21.1` | Required by MineColonies |
| **Multi-Piston** | `1.21.1` | Required by MineColonies |
| **Domum Ornamentum** | `1.21.1` | Required by MineColonies |

---

## 🚀 Installation

1. Install **Minecraft 1.21.1** with **NeoForge**.
2. Download and drop the following files into your `.minecraft/mods` folder:
   - `mcaminecolonies-1.0.0.jar`
   - `minecolonies-1.21.1-*.jar` (along with `structurize`, `blockui`, `multipiston`, `domum_ornamentum`)
   - `mca-neoforge-7.7.36-beta.3+1.21.1.jar`
3. Launch the game. Colony citizens will immediately spawn and render as full MCA humans!

---

## 🛠️ Building from Source

```bash
# Clone the repository
git clone https://github.com/mrzero784/MCA-x-Minecolonies-Integration.git
cd MCA-x-Minecolonies-Integration

# Build using Gradle Wrapper
./gradlew build
```

Compiled JAR file will be available at:
```
build/libs/mcaminecolonies-1.0.0.jar
```

---

## 📄 License & Credits

This mod is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
- **MineColonies** is developed by the MineColonies Team.
- **MCA Reborn (Minecraft Comes Alive)** is developed by the MCA Team.
- Special thanks to both development communities for their outstanding open-source frameworks.
