package com.enthusia.donors.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enthusia.donors.model.DonorEntry;
import com.enthusia.donors.model.RefreshState;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class DonorCacheTest {
    private static final UUID ALICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CAROL_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void replacementBuildsIndependentSortedAlltimeAndMonthlyLeaderboards() {
        DonorEntry alice = donor(ALICE_ID, "Alice", "10.00", "0", 2, 99);
        DonorEntry bob = donor(BOB_ID, "Bob", "20.00", "5.00", 1, 2);
        DonorEntry carol = donor(CAROL_ID, "Carol", "0", "9.00", 99, 1);
        DonorCache cache = new DonorCache();

        cache.replace(List.of(alice, bob, carol), Instant.parse("2026-01-02T03:04:05Z"), RefreshState.READY);

        assertEquals(List.of(bob, alice), cache.snapshot().alltime());
        assertEquals(List.of(carol, bob), cache.snapshot().monthly());
        assertEquals(bob, cache.snapshot().topAlltime(1).orElseThrow());
        assertEquals(carol, cache.snapshot().topMonthly(1).orElseThrow());
        assertTrue(cache.snapshot().topAlltime(0).isEmpty());
        assertTrue(cache.snapshot().topMonthly(3).isEmpty());
    }

    @Test
    void snapshotIndexesPlayersByUuidAndCaseInsensitiveName() {
        DonorEntry alice = donor(ALICE_ID, "Alice", "10", "2", 1, 1);
        DonorCache cache = new DonorCache();
        cache.replace(List.of(alice), Instant.EPOCH, RefreshState.READY);

        assertSame(alice, cache.snapshot().byUuid(ALICE_ID).orElseThrow());
        assertSame(alice, cache.snapshot().byName("alice").orElseThrow());
        assertSame(alice, cache.snapshot().byName("ALICE").orElseThrow());
        assertTrue(cache.snapshot().byName(null).isEmpty());
        assertTrue(cache.snapshot().byUuid(BOB_ID).isEmpty());
    }

    @Test
    void duplicateLookupKeysKeepTheFirstObservedEntry() {
        DonorEntry first = donor(ALICE_ID, "Alice", "10", "1", 1, 1);
        DonorEntry duplicate = donor(ALICE_ID, "ALICE", "20", "2", 2, 2);
        DonorCache cache = new DonorCache();

        cache.replace(List.of(first, duplicate), Instant.EPOCH, RefreshState.READY);

        assertSame(first, cache.snapshot().byUuid(ALICE_ID).orElseThrow());
        assertSame(first, cache.snapshot().byName("alice").orElseThrow());
    }

    @Test
    void refreshStateTransitionsRetainSafeErrorAndTimestampSemantics() {
        DonorCache cache = new DonorCache();
        assertEquals(RefreshState.STARTING, cache.state());
        assertEquals("", cache.lastError());

        cache.markAttempt();
        assertEquals(RefreshState.REFRESHING, cache.state());
        assertNotNull(cache.lastRefreshAttempt());

        cache.markFailure(null);
        assertEquals(RefreshState.TEBEX_FAILED, cache.state());
        assertEquals("", cache.lastError());

        cache.markFailure("safe failure");
        assertEquals("safe failure", cache.lastError());

        cache.markCacheOnly();
        assertEquals(RefreshState.CACHE_ONLY, cache.state());
        cache.markNotConfigured();
        assertEquals(RefreshState.TEBEX_NOT_CONFIGURED, cache.state());

        Instant success = Instant.parse("2026-02-03T04:05:06Z");
        cache.replace(List.of(), success, RefreshState.READY);
        assertEquals(RefreshState.READY, cache.state());
        assertEquals("", cache.lastError());
        assertEquals(success, cache.lastSuccessfulRefresh());
        assertFalse(cache.snapshot().updatedAt() == null);
    }

    @Test
    void rawAmountsAreStableAndHalfUpRounded() {
        DonorCache cache = new DonorCache();
        assertEquals("0.00", cache.rawAmount(null));
        assertEquals("1.23", cache.rawAmount(new BigDecimal("1.234")));
        assertEquals("1.24", cache.rawAmount(new BigDecimal("1.235")));
        assertEquals("-1.24", cache.rawAmount(new BigDecimal("-1.235")));
    }

    private static DonorEntry donor(
            UUID id,
            String name,
            String alltime,
            String monthly,
            int alltimeRank,
            int monthlyRank
    ) {
        return new DonorEntry(
                id,
                name,
                new BigDecimal(alltime),
                new BigDecimal(monthly),
                alltimeRank,
                monthlyRank,
                123L
        );
    }
}
