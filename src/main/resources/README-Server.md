# FeatherfallBank — Server Admin Notes

FeatherfallBank adds GTA-style shillings with two balances:
- **Pouch** (on the player) — quick access, partially drops on death
- **Treasury** (bank) — stored safely, accessed via bell GUI or `/pouch`

> Requires **Paper 1.21.x**, **Vault**, and a Vault-compatible economy plugin.

---

## Install
1. Drop `FeatherfallBank-*.jar` into `/plugins/`.
2. Ensure **Vault** and an economy plugin (e.g., EssentialsX Economy) are installed.
3. Start the server once to generate config + resources.

Files generated in `/plugins/FeatherfallBank/`:
- `config.yml` – all settings
- `messages.yml` – player-facing text (MiniMessage)
- `README-Server.md` – this file

---

## Quickstart (bell whitelist mode)
By default we use a **bell coordinate whitelist** so only specific bells open the bank.

Edit `config.yml`:

```yaml
gui:
  open_via:
    teller_block_types: ["BELL"]
    require_worldguard_region: false
    regions: []
    bell_whitelist:
      enabled: true
      list:
        - world: "world"
          x: 123
          y: 64
          z: -321
