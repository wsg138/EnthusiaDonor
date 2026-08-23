# EnthusiaDonors — SMP Player Guide

This file documents the player-visible donor/support leaderboard behavior used by Enthusia SMP. The main [`README.md`](README.md) remains the technical Tebex, privacy, export, and administrative reference.

The production values below were checked against the live server configuration and current source on August 22, 2026.

## What this plugin does for players

EnthusiaDonors turns completed Tebex purchases into public-safe **support leaderboards**. It does not grant donor ranks itself and it does not calculate spending by looking at a player's LuckPerms rank or permissions.

The player-visible data is consumed through PlaceholderAPI, the website leaderboard export, and related displays such as Enthusia's donor NPC system.

There is no normal player `/enthusiadonors` management command; those commands are staff/operator tools.

## What counts toward support totals

The source of truth is **Tebex payment history**.

Current production rules:

- completed/paid purchases with a value above zero count;
- zero-dollar payments do not count;
- manual payments do not count;
- refunded payments do not count;
- chargebacks do not count;
- failed, pending, cancelled, denied, or voided payments do not count;
- there is currently no package allowlist or package denylist, so otherwise-valid purchases are eligible regardless of package;
- a small private list of known test/administrative transaction IDs is excluded from totals.

The plugin currently does **not** add available taxes/fees to the displayed support amount.

This means the leaderboard is intended to represent valid money actually paid through Tebex, not what rank somebody currently owns.

## All-time and monthly boards

The donor data includes both:

- **all-time support**; and
- **monthly support**.

The production leaderboard timezone is **America/Chicago**. The configured public board size is **10 players** and amounts are displayed in dollars with cents (for example `$25.00`).

The Tebex source refreshes automatically every **10 minutes**. If a refresh fails, the plugin keeps the last valid cached leaderboard instead of clearing everyone to zero.

The public website export currently publishes the all-time donor board for the Enthusia website. Other server displays can use the cached all-time/monthly PlaceholderAPI values directly.

## Privacy

The public leaderboard intentionally does **not** expose raw Tebex purchase records.

Public-safe output is limited to information such as:

- Minecraft UUID;
- Minecraft username/display name;
- leaderboard rank;
- all-time/monthly support amount;
- last-update information.

It does not publish:

- email addresses;
- Tebex transaction IDs;
- IP addresses;
- gateway/payment-provider details;
- notes;
- raw payment objects.

Configured excluded transaction IDs also stay private rather than being included in public JSON.

## Kill/death leaderboard data

This plugin also maintains cached Minecraft kill/death statistics because existing Enthusia displays use those PlaceholderAPI values alongside donor data.

It imports known Bukkit statistics and updates them from live deaths. This does **not** mean kills/deaths affect donor totals; they are separate leaderboard data exposed by the same plugin.

Top-kill and top-death placeholders are available for ranks 1–10, along with a player's own cached kill/death counts.

## PlaceholderAPI

The PlaceholderAPI identifier is `enthusiadonors`.

The repository exposes donor, monthly/all-time, rank, kill, and death leaderboard values for server displays. Examples already used/documented include:

```text
%enthusiadonors_kills_top_1_name%
%enthusiadonors_kills_top_1_kills%
%enthusiadonors_deaths_top_1_name%
%enthusiadonors_deaths_top_1_deaths%
%enthusiadonors_kills_count%
%enthusiadonors_deaths_count%
```

See `PlaceholderHook.java` for the complete exact donor placeholder names when wiring new holograms/NPCs/menus; the public wiki generally does not need to list every implementation placeholder.

## What belongs on the public wiki

Useful player-facing information includes:

- the existence of all-time/monthly donor leaderboards;
- what payments count and what refunds/chargebacks do not;
- the 10-minute refresh cadence;
- that donor totals come from Tebex payments rather than ranks;
- what public information is shown;
- where players can view the leaderboard (website/NPC/display pages once those are documented from their owning systems).

Tebex API credentials, excluded transaction IDs, retry internals, SQLite schema, R2 upload details, and staff debug commands should remain private/technical documentation.
