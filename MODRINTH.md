# MCA x MineColonies Integration

A seamless integration mod for **Minecraft 1.21.1 (NeoForge)** that completely bridges **MineColonies** and **MCA Reborn (Minecraft Comes Alive)**.

Tired of the standard blocky, big-nosed villagers running around your colony? Want your citizens to look like real, unique human beings with distinct hairstyles, eyes, personalities, dialogue trees, and family progression — all while still being hard-working MineColonies builders, miners, and guards? 

**MCA x MineColonies** makes that dream a reality!

---

## ✨ Features

### 👤 1. Full MCA 3D Humanoid Models & Genetics
- **Replaced Citizen Visuals**: All colony citizens use MCA Reborn's 3D player-like humanoid models.
- **Gendered Geometry**:
  - **Female Citizens**: Alex slim 3-pixel arms, female faces, female hairstyles, and feminine profession outfits.
  - **Male Citizens**: Steve 4-pixel arms, male faces, masculine hairstyles, and masculine profession outfits.
- **Layered 3D Hair**: Multi-part hairstyles (Bangs, Base, Back, Front, Extra) with individual RGB hair dyeing.
- **Heterochromia / Dual Eye Colors**: Separate left and right eye color genetics.
- **Realistic Skin Palettes**: Uses MCA's melanin and hemoglobin system for realistic skin tones.
- **Profession Outfits**: Automatically adapts MineColonies profession uniforms onto MCA's clothing models.

---

### 👶 2. Natural Growth & Life Stages
- **5 Realistic Life Stages**:
  - 🍼 **Baby**: Cute swaddle crawl stage.
  - 🧸 **Toddler**: Early childhood with cute proportions.
  - 🎒 **Child**: Active child stage with child outfits.
  - 🎧 **Teen**: Youth stage preparing for adult colony careers.
  - 🧑‍💼 **Adult**: Full adult citizen ready for workplace assignments.
- **Dynamic Proportions & Hitboxes**: Citizen collision boxes and eye heights scale accurately with age.
- **Accelerated Growth**: Feed children Cake, Golden Apples, Apples, or Cookies to help them grow faster.
- **Automatic Graduation**: Growing into adulthood automatically makes the citizen available for any MineColonies job!

---

### 💬 3. Interactive Dialogue & Fatigue System
- **In-GUI Talk Button**: Open any citizen's MineColonies window (`MainWindowCitizen`) and click **💬 Talk** in the top-right corner to open MCA's `InteractScreen`.
- **Multi-Branch Conversations**: Chat, Joke, Story, Flirt, Kiss, Hug, Play (Rock-Paper-Scissors), Apologize, Rumors, and Hire.
- **Personality-Driven Responses**: Responses reflect the citizen's unique traits, mood, and relationship with you.
- **Fatigue & Anti-Spam Mechanics**:
  - Citizens build conversation fatigue. If spoken to excessively (`Fatigue >= 4`), they will ask for space until rested.
  - Fatigue naturally cools down over time according to MCA's cooldown configs.
- **Enhanced UI Controls**:
  - Responses appear directly inside the speech bubble.
  - **Click-to-Dismiss**: Once the citizen delivers their final line, left-click anywhere on the screen to close the bubble and return to the main interaction menu (no need to reach for `ESC`!).

---

### 🎁 4. Authentic MCA Gifting & Saturation
- **No More Infinite Gifting**: Fully adheres to MCA Reborn's balanced gift mechanics.
- **Dynamic Value Evaluation**:
  - Swords & Axes scored by damage.
  - Bows & Crossbows scored by range.
  - Tools scored by mining speed.
  - Armor scored by protection & toughness.
  - Foods scored by nutrition.
- **Gift Saturation Penalty**: Giving the same item repeatedly triggers diminishing returns. Spammed items will be rejected with `"gift.saturated"` without wasting your items!
- **Trait Satisfaction Analysis**: Citizens analyze gifts according to their personality and display an `AnalysisResults` popup.
- **Inventory Integration**: Accepted gifts consume 1 item from hand, insert it into the citizen's colony inventory, and award hearts/mood.

---

### 💍 5. Marriage, Family & Genetics Inheritance
- **MCA Relationship Items**:
  - 💍 **Engagement Ring & Wedding Ring**: Propose and marry colony citizens.
  - 💐 **Bouquets**: Express affection and boost romantic relationship status.
  - 📜 **Divorce Papers**: Cleanly dissolve marriages.
- **Genetics Inheritance**: Children born from colony reproduction inherit hair colors, eye colors, and skin tones from their colony parents.

---

### ✂️ 6. In-Game Customization & Villager Editor
- **Villager Editor & Dressing Mirror**: Freely modify age, hair, dyes, skin tone, clothing, and gender with real-time updates.
- **Handheld MCA Styling Tools**:
  - 💇 **Comb / Shears**: Cycle through hairstyles with clicking sounds.
  - 🧵 **Needle & Thread**: Cycle through outfits.
  - 🧪 **Potion of Femininity / Masculinity**: Switch citizen gender.
  - 🎨 **Dyes**: Change citizen hair color in-world.

---

### 🏰 7. 100% MineColonies Worker Compatibility
- Fully maintains all MineColonies AI, job assignments, pathfinding, building, guarding, and farming.
- Tavern visitors and wandering recruits are protected to stay in their Recruit screen as intended.

---

## 📦 Requirements

This mod requires **Minecraft 1.21.1** with **NeoForge**:

- **NeoForge** `21.1.x` or higher
- **MineColonies** `1.21.1` (and its dependencies: `Structurize`, `BlockUI`, `Multi-Piston`, `Domum Ornamentum`)
- **MCA Reborn (Minecraft Comes Alive)** `7.7.36-beta.3+1.21.1` or higher

---

## ❓ Frequently Asked Questions (FAQ)

#### Q: Can I add this mod to an existing world or colony?
**A:** Yes! Any existing MineColonies citizens will automatically update their visuals to MCA models on chunk load.

#### Q: Do citizens still do their MineColonies jobs?
**A:** Yes, 100%! All worker routines, pathfinding, building, farming, mining, and guard tasks operate with zero disruptions.

#### Q: Can I marry a MineColonies citizen?
**A:** Yes! You can interact, give gifts, propose with an Engagement Ring, and marry them using MCA Reborn's relationship items.

#### Q: Does it support MineColonies addons like MineColonies Mages?
**A:** Yes! Standalone and modular design: runs smoothly on its own without requiring any optional addon mods.

#### Q: Does Creative Mode bypass conversation limits?
**A:** No, MCA Reborn's fatigue and gift saturation rules apply universally to provide a balanced and immersive roleplay experience.

---

## 📜 License

Licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
Compatible with both MineColonies and MCA Reborn open-source licenses.

