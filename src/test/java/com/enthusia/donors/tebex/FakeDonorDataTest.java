package com.enthusia.donors.tebex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enthusia.donors.model.PaymentRecord;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

final class FakeDonorDataTest {
    @Test
    void createsRequestedNumberOfCompleteUsdPayments() {
        List<PaymentRecord> payments = FakeDonorData.payments(3);

        assertEquals(3, payments.size());
        for (int i = 0; i < payments.size(); i++) {
            PaymentRecord payment = payments.get(i);
            int number = i + 1;
            assertEquals("fake-" + number, payment.transactionId());
            assertEquals("TestDonor" + number, payment.playerName());
            assertEquals("USD", payment.currency());
            assertEquals("Complete", payment.status());
            assertFalse(payment.refunded());
            assertFalse(payment.chargeback());
            assertEquals(List.of(1000 + number), payment.packageIds());
        }
    }

    @Test
    void fakeIdentitiesAreStableAcrossCalls() {
        List<PaymentRecord> first = FakeDonorData.payments(2);
        List<PaymentRecord> second = FakeDonorData.payments(2);
        assertEquals(first.get(0).playerUuid(), second.get(0).playerUuid());
        assertEquals(first.get(1).playerUuid(), second.get(1).playerUuid());
    }

    @Test
    void amountsDescendBySevenDollarsAndFiftyCents() {
        List<PaymentRecord> payments = FakeDonorData.payments(3);
        assertEquals(new BigDecimal("22.5"), payments.get(0).amount());
        assertEquals(new BigDecimal("15.0"), payments.get(1).amount());
        assertEquals(new BigDecimal("7.5"), payments.get(2).amount());
    }

    @Test
    void zeroOrNegativeCountsProduceNoPayments() {
        assertTrue(FakeDonorData.payments(0).isEmpty());
        assertTrue(FakeDonorData.payments(-1).isEmpty());
    }
}
