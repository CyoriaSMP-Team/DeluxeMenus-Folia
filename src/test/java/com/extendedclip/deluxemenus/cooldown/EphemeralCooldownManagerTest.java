package com.extendedclip.deluxemenus.cooldown;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EphemeralCooldownManagerTest {
    @Test
    void normalizesIdsAndExpiresOnClear() {
        EphemeralCooldownManager manager = new EphemeralCooldownManager(null);
        UUID player = UUID.randomUUID();

        manager.set(player, " Menu:Daily ", 60_000);

        assertTrue(manager.isOnCooldown(player, "menu:daily"));
        assertTrue(manager.getRemainingMillis(player, "MENU:DAILY") > 0);
        manager.clear(player, "menu:daily");
        assertFalse(manager.isOnCooldown(player, " Menu:Daily "));
    }

    @Test
    void concurrentUpdatesRemainReadable() throws Exception {
        EphemeralCooldownManager manager = new EphemeralCooldownManager(null);
        UUID player = UUID.randomUUID();
        int workers = 4;
        CountDownLatch done = new CountDownLatch(workers);
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        try {
            for (int i = 0; i < workers; i++) {
                final int id = i;
                pool.execute(() -> {
                    manager.set(player, "cooldown-" + id, 60_000);
                    assertTrue(manager.isOnCooldown(player, "COOLDOWN-" + id));
                    done.countDown();
                });
            }
            assertTrue(done.await(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
    }
}
