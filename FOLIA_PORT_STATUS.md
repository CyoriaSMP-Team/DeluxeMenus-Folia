# Folia 1.21.11 port status

The `folia/1.21.11` branch now routes GUI and player-facing work through entity-owned scheduling, uses the global scheduler for console dispatch and broadcasts, and retains a Paper scheduler fallback. Placeholder parsing during menu construction, refresh, and periodic item updates runs with the viewer's entity-owned inventory work. Menu YAML formats and the plugin name are unchanged.

The plugin is intentionally not marked `folia-supported: true`. That marker remains absent until full builds and live Paper/Folia integration checks establish safety.

PlaceholderAPI is evaluated on the viewer's entity scheduler, alongside its inventory updates. Generic PlaceholderAPI expansions do not provide a universal guarantee that player-backed parsing is safe on an arbitrary async thread, so this port does not move all placeholder evaluation to the async scheduler.

## Not yet verified

- Full Gradle compile and test execution; local Gradle builds were skipped because of the production RAM constraint. CI is configured to execute these checks.
- Behavior of arbitrary PlaceholderAPI expansions and external requirement/item integrations, which may impose their own thread-affinity assumptions.
- `placeholders_for` where the placeholder player differs from the menu viewer.
- Third-party command handlers invoked by player or console action dispatch.
- Shutdown cleanup, active-viewer reload ordering, and scheduler retirement on a running Folia server.
- Cross-region sound and message broadcasts in a live server.
- Command tab completion and optional hooks such as Vault, HeadDatabase, ItemsAdder, Nexo, Oraxen, CraftEngine, and MMOItems under Folia.

See [agent-logs/deluxemenus-result.md](agent-logs/deluxemenus-result.md) for the implementation summary and verification record.
