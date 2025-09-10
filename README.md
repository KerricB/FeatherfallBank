```markdown
# FeatherfallBank

[![Build](https://img.shields.io/badge/build-Maven-blue)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Modrinth](https://img.shields.io/badge/Modrinth-FeatherfallBank-1bd96a)](#)

A lightweight Vault economy plugin built for modern Paper servers that introduces **Shillings** as currency, a player **Pouch** wallet, and a region‑safe **Treasury** bank with polished GUIs.

> **Design goals:** Simple setup, fun die‑drop mechanics, clear UX, and server‑friendly anti‑abuse protections.

---

## ✨ Features
- **Vault Economy Provider** — Registers a currency named **Shillings**, compatible with popular plugins via Vault.
- **Pouch (wallet)** — A physical/virtual wallet with clear action‑bar pickup messages and itemized drops.
- **Treasury (bank)** — Secure balance separate from the pouch; open via command or Teller block.
- **Polished GUIs** — Clean Deposit/Withdraw menus (no item pickup exploits), responsive sound/feedback.
- **Fair Death Penalty** — Drops **50%** of the Pouch on *any* death; configurable and safe‑zoned.
- **Safe Zones** — Prevent drops in defined regions (e.g., spawn) with WorldGuard integration.
- **Anti‑Abuse** — Pouches are hopper‑proof & fire‑proof; despawned pouch items are safely discarded.
- **SQLite Storage** — Zero‑config default; future DB options planned.
- **Admin Tools** — `/treasuryadmin set|give|take|top`, live reloading, and locale‑aware formatting.

---

## ✅ Requirements
- **Server:** PaperMC 1.21.x (tested on 1.21.8)
- **Dependencies:**
  - **Vault** (required)
  - **WorldGuard** (optional, for safe‑zone integration)
  - **Citizens** (optional, planned Teller NPC integration)

> If you currently use **EssentialsX Economy**, remove the `EssentialsX-Economy` jar so FeatherfallBank is the sole Vault economy provider.

---

## 🚀 Installation
1. Install **Vault** and (optionally) **WorldGuard**.
2. Drop `FeatherfallBank-x.y.z.jar` into your `plugins/` folder.
3. Start the server to generate config files.
4. Verify with `/vault-info` that **FeatherfallBank** is the registered economy.
5. (Optional) Configure safe regions and starting balance (see below), then `/treasuryadmin reload`.

---

## 🔧 Configuration (config.yml)
```yml
# FeatherfallBank main configuration
currency-name: "Shillings"
currency-name-singular: "Shilling"
starting-balance: 500

formatting:
  # e.g. 12,345 Shillings
  use-locale-formatting: true

pouch:
  # Percentage of the pouch balance dropped on death (0.0 - 1.0)
  death-drop-percent: 0.5
  # If keepInventory is true, skip drops entirely
  respect-keep-inventory: true
  # Prevent hoppers from extracting pouch items
  hopper-proof: true
  # If a pouch item somehow despawns, delete rather than duping/crediting
  despawn-deletes: true

safe-zones:
  enabled: true
  # WorldGuard region names where pouch drops are disabled (no penalties)
  regions:
    - owlrun
    - spawn

# Treasury access
treasury:
  # Players can open via command regardless of region
  allow-command-open: true
  teller:
    enabled: true
    # Require player to stand in a region to open via teller block
    require-region: true
    regions:
      - owlrun
    # Block types that count as Tellers (choose one or add multiple)
    blocks:
      - LECTERN
      - BELL

# Admin & data
storage:
  type: SQLITE

messages:
  pickup: "+{amount} Shillings to your Pouch."
  receipt-deposit: "Deposited {amount} Shillings to your Treasury."
  receipt-withdraw: "Withdrew {amount} Shillings to your Pouch."
```

---

## 🕹️ Commands & Permissions

### Player
- `/pouch` — Open the Pouch GUI.  
  **Permission:** `featherfallbank.pouch`
- `/pouch drop <amount|all>` — Drop Shillings from your pouch as items.  
  **Permission:** `featherfallbank.pouch.drop`
- `/treasury` — Open the Treasury (bank) GUI.  
  **Permission:** `featherfallbank.treasury`

### Admin
- `/treasuryadmin set <player> <amount>` — Set Treasury balance.  
  **Permission:** `featherfallbank.admin.set`
- `/treasuryadmin give <player> <amount>` — Add to Treasury.  
  **Permission:** `featherfallbank.admin.give`
- `/treasuryadmin take <player> <amount>` — Remove from Treasury.  
  **Permission:** `featherfallbank.admin.take`
- `/treasuryadmin top [page]` — Leaderboard of Treasury balances.  
  **Permission:** `featherfallbank.admin.top`
- `/treasuryadmin reload` — Reload config/messages.  
  **Permission:** `featherfallbank.admin.reload`

> You can grant `featherfallbank.admin.*` to trusted staff.

---

## 🔒 Mechanics at a Glance
- **Death Drops:** By default, 50% of *current Pouch* is dropped as Shilling items on any death.
- **Safe Zones:** In configured regions (e.g., `owlrun`), deaths **do not** drop pouch contents.
- **Keep Inventory:** If `keepInventory` is active and `respect-keep-inventory: true`, no pouch drops occur.
- **Pickup UX:** Action‑bar feedback confirms how much was added to the Pouch.
- **Anti‑Abuse:** Hopper‑proof pouch items, safe despawn handling, and GUI click protection.

---

## 🧩 Compatibility
- **Vault** economy bridge ensures broad support with shops, ranks, and other economy‑aware plugins.
- If migrating from another economy, use admin commands to seed balances and set a **starting-balance** for new players.

---

## 🧭 Roadmap
- Citizens teller NPCs
- PlaceholderAPI placeholders
- MySQL/PostgreSQL storage option
- Audit log for deposits/withdrawals

---

## 🧑‍💻 Building from Source
- **Java 21** recommended
- Build with **Maven**: `mvn -q -DskipTests package` (or Gradle if you prefer)
- Output jar: `target/FeatherfallBank-<version>.jar`

Project layout:
```
.
├─ src/main/java/
├─ src/main/resources/
│  └─ plugin.yml
├─ pom.xml
└─ README.md
```

---

## 🤝 Contributing
PRs and issues welcome! Please open an issue for feature requests or bugs and follow the style of existing code.

---

## 📜 License
This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
```
