# EnthusiaDonor testing guide

This repository owns its handwritten unit/regression tests. Sentinel Sim may consume the built plugin later for compatibility/lifecycle evidence, but ordinary Donor behavior tests stay with this repository and must pass independently.

## What the current test-hardening PR adds

The owner-directed test branch establishes the repository's first normal JUnit 5 regression harness and adds:

- `src/test/java/com/enthusia/donors/cache/DonorCacheTest.java`
- `src/test/java/com/enthusia/donors/tebex/FakeDonorDataTest.java`

`pom.xml` is changed only as required to include/run the JUnit 5 tests in the normal Maven lifecycle.

No runtime/plugin behavior is intentionally changed by this test PR.

## What is covered

### Donor cache and leaderboard behavior

`DonorCacheTest` covers:

- independent all-time and monthly filtering/sorting;
- top-rank bounds;
- UUID lookup;
- case-insensitive player-name lookup;
- duplicate UUID/name first-observation semantics;
- refresh attempt/failure/cache-only/not-configured/ready state transitions;
- safe error clearing/retention;
- successful refresh timestamps;
- stable raw amount formatting;
- HALF_UP currency rounding including negative values.

These tests protect the state users actually see through donor leaderboards/placeholders from regressions that would otherwise be easy to miss.

### Deterministic fake Tebex data

`FakeDonorDataTest` covers:

- requested payment count;
- deterministic transaction IDs/player names;
- stable player UUIDs across calls;
- USD currency;
- complete/non-refunded/non-chargeback flags;
- deterministic package IDs;
- descending amounts;
- zero/negative requested counts returning no payments.

The fake-data contract matters because local/testing modes must be deterministic; otherwise cache/leaderboard tests can become flaky and difficult to review.

## How to run the tests

Run the full unit suite:

```bash
mvn test
```

Run the full verification lifecycle:

```bash
mvn clean verify
```

Run one focused class while editing:

```bash
mvn -Dtest=DonorCacheTest test
mvn -Dtest=FakeDonorDataTest test
```

## Where results are written

Maven Surefire writes reports under:

- `target/surefire-reports/`

GitHub Actions is the durable exact-head source for reviewed CI/artifact evidence. The artifact workflow's Maven verification must execute these tests before an artifact is treated as proven.

A passing workflow on an older commit is stale after any new commit.

## How to interpret failures

### Leaderboard ordering/filter failure

Verify whether the product's all-time/monthly eligibility and ordering contract intentionally changed. Do not simply change expected order to match an accidental sort/filter regression.

### Lookup/index failure

Check UUID identity, name normalization/case handling, and duplicate-key semantics. A duplicate handling change can alter which donor a placeholder resolves, so it must be deliberate and documented.

### Refresh-state failure

Treat state-machine failures as user/operator-visible behavior. Verify transitions for successful refresh, API failure, cache-only operation, missing Tebex configuration, safe errors, and timestamps.

### Currency formatting/rounding failure

Confirm the documented money-display contract. Avoid floating-point replacement for the current `BigDecimal` semantics unless the product contract deliberately changes.

### Fake-data failure

Fake data should remain deterministic. Randomized IDs/amounts/timing make regression tests unreliable and should not be introduced without a separate controlled seeded test design.

### Maven/setup failure

Compiler/dependency/Surefire failures are test-harness/build failures, not product behavior evidence. Fix the harness/build and rerun the same exact head.

## What is not covered yet

This first suite focuses on deterministic cache/model/fake-provider behavior. It does not by itself prove:

- live Tebex HTTP/API behavior;
- credentials/secrets handling against a real service;
- Paper plugin enable/disable/reload lifecycle;
- PlaceholderAPI registration and live placeholder expansion;
- persistence/database behavior if/when added;
- cross-plugin/provider compatibility;
- real production timing/network behavior.

Those areas should get repository integration tests using fakes/disposable services where possible, and Sentinel/real-Paper acceptance when an actual server/dependency boundary is required. Never put live Tebex credentials or production donor data into tests or CI artifacts.

## Adding tests for new behavior

When the Donor plugin changes:

1. identify the deterministic product contract that changed;
2. add/update focused repository-local behavioral tests;
3. add failure/degraded/provider-missing paths where relevant;
4. keep fake data deterministic and explicit;
5. use disposable/fake HTTP/provider infrastructure rather than live production services;
6. run `mvn test` during development;
7. run `mvn clean verify` for handoff;
8. verify final CI belongs to the exact reviewed head;
9. update this guide if test layout, commands, or known boundaries change;
10. reconcile the Sentinel profile separately when the built plugin's runtime/dependency surface changes.

## Reviewer checklist

- [ ] Leaderboard eligibility and sort assertions reflect intentional product behavior.
- [ ] UUID/name duplicate semantics are explicit.
- [ ] Refresh/failure states cover safe degraded operation.
- [ ] Currency math remains deterministic and precise.
- [ ] Fake data is stable and contains no production/private information.
- [ ] No live Tebex credential or mutable external service is required by unit tests.
- [ ] Runtime code was not weakened just to make tests pass.
- [ ] `mvn clean verify` passes on the exact final head.
- [ ] Any untested Paper/Tebex/runtime behavior is stated honestly.

## Sentinel Sim boundary

Do not move these JUnit tests into Sentinel Sim. Sentinel should consume an already repository-tested artifact and add higher-level runtime, dependency, compatibility, sequence/fuzz, and real-Paper evidence where appropriate.
