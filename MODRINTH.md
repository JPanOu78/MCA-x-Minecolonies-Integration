# MCA x MineColonies Integration Bridge

A dedicated compatibility bridge for **Minecraft 1.21.1 (NeoForge)** connecting **MineColonies** and **MCA Reborn (Minecraft Comes Alive)**.

Instead of running as two separate systems, this mod builds a bridge between them: **MineColonies** handles all colony management, worker jobs, pathfinding, and town expansion, while **MCA Reborn** powers 3D humanoid visuals, genetics, life stages, dialogue trees, and balanced gifting.

---

## 🌉 What This Bridge Does

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

### 👤 1. Visual & Model Bridge
*Bridges MineColonies citizen rendering to MCA's player-like humanoid models.*
- **Custom Render Pipeline**: Intercepts `EntityCitizen` rendering and binds MCA's 3D humanoid models (Alex 3px slim for females, Steve 4px standard for males).
- **Genetics Attachment**: Attaches MCA genetics to MineColonies citizens, generating unique multi-layered hair (RGB-dyed), heterochromia (dual eye colors), and natural melanin/hemoglobin skin tones.
- **Dynamic Uniform Mapping**: Translates MineColonies profession uniforms onto MCA's clothing geometry, adjusting dynamically based on citizen gender and age stage.
- **Editor & Tool Sync**: Connects MCA styling tools (Comb, Shears, Needle & Thread, Dyes, Gender Potions) and the MCA Villager Editor GUI directly to MineColonies citizens, synchronizing visual data in real-time.

---

### 👶 2. Age & Growth Bridge
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

### 💬 3. Dialogue & UI Bridge
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

## 📦 Requirements

- **Minecraft**: `1.21.1`
- **NeoForge**: `21.1.x` or higher
- **MineColonies**: `1.21.1` (with `Structurize`, `BlockUI`, `Multi-Piston`, `Domum Ornamentum`)
- **MCA Reborn**: `7.7.36-beta.3+1.21.1` or higher

---

## ❓ Frequently Asked Questions (FAQ)

#### Q: Can I add this mod to an existing world or colony?
**A:** Yes! Existing MineColonies citizens will automatically update their visuals to MCA models on chunk load.

#### Q: Why do young children walk instead of lying in swaddles?
**A:** In MineColonies, child citizens are active community members that pathfind, run around, eat, and sleep in beds. To preserve full colony AI without breaking pathfinding, the youngest age stage is an **active walking Toddler** with cute proportions and walking animations, progressing naturally into Child, Teen, and Adult.

#### Q: Do citizens still do their MineColonies jobs?
**A:** Yes, 100%! All worker routines, pathfinding, building, farming, mining, and guard tasks operate with zero disruptions.

#### Q: Can I inspect stats for tavern visitors before hiring?
**A:** Yes! You can click the standard "Show citizen stats" button directly inside the visitor recruitment dialog.

#### Q: Does it require addons like MineColonies Mages or Iron's Spells?
**A:** No. The mod is completely modular and standalone. It runs with just MineColonies and MCA Reborn, while remaining fully compatible if you choose to install combat or magic addons.

---

## 📜 License

Licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
Compatible with both MineColonies and MCA Reborn open-source licenses.
