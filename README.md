# MCA x MineColonies: Integration Bridge

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/ModLoader-NeoForge-orange.svg)](https://neoforged.net/)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A dedicated compatibility bridge for **Minecraft 1.21.1 (NeoForge)** that connects **MineColonies** with **MCA Reborn (Minecraft Comes Alive)**.

Rather than running as two disconnected systems, this mod builds a bidirectional bridge: MineColonies manages all citizen AI, colony jobs, building, and pathfinding, while MCA Reborn drives citizen rendering, genetics, age scaling, dialogue trees, and balanced social interactions.

---

## 🌉 What the Bridge Does

```
┌─────────────────────────────────┐               ┌─────────────────────────────────┐
│          MineColonies           │               │           MCA Reborn            │
│  - Colony AI & Pathfinding      │ ◄───────────► │  - 3D Humanoid Models           │
│  - Citizen Jobs & Workplaces    │   INTEGRATION │  - Hair, Eyes & Skin Genetics   │
│  - Village Growth & Housing     │     BRIDGE    │  - Dialogue & Fatigue Systems   │
│  - Citizen Inventory & Stats    │               │  - Balanced Gifting Formulas    │
└─────────────────────────────────┘               └─────────────────────────────────┘
```

---

### 1. 👤 Visual & Model Bridge
*Bridges MineColonies citizen rendering to MCA's player-like humanoid models.*
- **Custom Render Pipeline**: Intercepts `EntityCitizen` rendering and binds MCA's 3D humanoid models (Alex 3px slim for females, Steve 4px standard for males).
- **Genetics Attachment**: Attaches MCA genetics to MineColonies citizens, generating unique multi-layered hair (RGB-dyed), heterochromia (dual eye colors), and natural melanin/hemoglobin skin tones.
- **Dynamic Uniform Mapping**: Translates MineColonies profession uniforms onto MCA's clothing geometry, adjusting dynamically based on citizen gender and age stage.
- **Editor & Tool Sync**: Connects MCA styling tools (Comb, Shears, Needle & Thread, Dyes, Gender Potions) and the MCA Villager Editor GUI directly to MineColonies citizens, synchronizing visual data in real-time.

---

### 2. 👶 Age & Growth Bridge
*Adapts MCA's life stages to preserve MineColonies' active child AI.*
- **Mobile Toddler Adaptation**: In vanilla MCA, babies are immobile swaddles in cribs. Because MineColonies children require active pathfinding to walk around town, eat at restaurants, sleep in beds, and attend school, the bridge maps the earliest life stage to an **active walking Toddler**:
  - **Toddler** (Youngest stage): Compact humanoid scale with cute proportions and walking animations; fully mobile in the colony.
  - **Child**: Scaled child model with casual outfits, roaming the colony.
  - **Teen**: Scaled adolescent model preparing for adult assignments.
  - **Adult**: Full-size citizen capable of taking on any colony profession.
- **Dynamic Hitbox Scaling**: Scales citizen collision boxes and eye heights in real-time according to MCA's `VillagerDimensions`.
- **Growth Acceleration Link**: Bridges food items (Cake, Golden Apples, Apples, Cookies) to directly advance MineColonies child growth timers.
- **Worker Graduation**: Automatically transitions matured citizens into MineColonies adult worker pools upon reaching adulthood.

---

### 3. 💬 Dialogue & UI Bridge
*Connects MineColonies citizen windows to MCA's interaction screen and packet protocols.*
- **In-GUI Talk Button**: Injects a **💬 Talk Button** into MineColonies' `MainWindowCitizen` that opens MCA's native `InteractScreen`.
- **Network Packet Translation**: Intercepts MCA dialogue network packets (`InteractionDialogueInitMessage`, `InteractionDialogueMessage`) on the server and redirects them to MineColonies citizen entities.
- **Fatigue Enforcement**: Bridges MCA's interaction fatigue tracker (`data.getFatigue()`). Citizens who are exhausted (`Fatigue >= 4`) politely refuse conversations until rested.
- **Click-to-Dismiss UI**: Allows players to dismiss speech bubbles by left-clicking anywhere on screen without pressing `ESC`.

---

### 4. 🎁 Gifting & Economy Bridge
*Replaces unrestricted citizen gifting with MCA's satisfaction algorithms.*
- **Dynamic Scoring**: Intercepts gift actions and passes items through MCA's item scoring engine (evaluating weapon damage, tool speed, armor protection, and food nutrition).
- **Trait Analysis**: Citizens evaluate gifts based on their MCA personality traits and return detailed `AnalysisResults`.
- **Inventory Transfer**: Valid gifts are consumed from the player's hand and placed into MineColonies `InventoryCitizen`, granting mood and heart particles.
- **Anti-Spam Saturation**: Enforces MCA's diminishing-returns saturation queue; spammed items are rejected (`"gift.saturated"`) without wasting or deleting the player's items.

---

### 5. 🏰 UI Compatibility & Modularity Bridge
*Resolves UI conflicts between the two mods and ensures clean modularity.*
- **Tavern Visitor Protection**: Prevents Tavern recruit interaction windows from prematurely falling back to `MainWindowCitizen` when exiting or pressing `ESC`.
- **Native Stats Passthrough**: Bridges MineColonies' native **"Show citizen stats"** button inside visitor recruitment dialogs, allowing players to inspect skills before hiring.
- **Standalone Architecture**: Operates cleanly without requiring third-party combat or magic addons (such as MineColonies Mages or Iron's Spells).

---

## 📋 Requirements

| Mod | Required Version | Note |
|---|---|---|
| **Minecraft** | `1.21.1` | Java Edition |
| **NeoForge** | `21.1.x`+ | Mod Loader |
| **MineColonies** | `1.21.1` | Colony Management (and its dependencies: `Structurize`, `BlockUI`, `Multi-Piston`, `Domum Ornamentum`) |
| **MCA Reborn** | `7.7.36-beta.3`+ | Minecraft Comes Alive Reborn |

---

## 🚀 Installation

1. Install **Minecraft 1.21.1** with **NeoForge**.
2. Place `mcaminecolonies-1.0.0.jar` along with **MineColonies** and **MCA Reborn** in your `.minecraft/mods` folder.
3. Launch the game. Existing and newly spawned colony citizens will automatically render and interact through the bridge.

---

## 🛠️ Building from Source

```bash
git clone https://github.com/mrzero784/MCA-x-Minecolonies-Integration.git
cd MCA-x-Minecolonies-Integration
./gradlew build
```

Compiled JAR will be at `build/libs/mcaminecolonies-1.0.0.jar`.

---

## 📄 License & Credits

Licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
- **MineColonies** is developed by the MineColonies Team.
- **MCA Reborn** is developed by the MCA Team.
