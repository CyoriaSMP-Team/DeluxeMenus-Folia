package com.extendedclip.deluxemenus.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;

import java.util.concurrent.TimeUnit;

/** Small scheduling boundary: entity work follows entity ownership, server-wide work uses
 * the global region, and CPU-only work uses the async scheduler. */
public final class FoliaScheduler {
    private FoliaScheduler() {}

    public interface Task {
        void cancel();
    }

    private static boolean isFolia() {
        return ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));
    }

    public static void entity(Plugin plugin, Entity entity, Runnable work) {
        if (!isFolia() || Bukkit.isOwnedByCurrentRegion(entity)) {
            work.run();
            return;
        }
        if (isFolia()) {
            entity.getScheduler().run(plugin, ignored -> work.run(), null);
        }
    }

    public static void entityLater(Plugin plugin, Entity entity, Runnable work, long ticks) {
        if (isFolia()) {
            entity.getScheduler().runDelayed(plugin, ignored -> work.run(), null, Math.max(1, ticks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, work, ticks);
        }
    }

    public static Task entityRepeating(Plugin plugin, Entity entity, Runnable work, long initialTicks, long periodTicks) {
        if (isFolia()) {
            var task = entity.getScheduler().runAtFixedRate(plugin, ignored -> work.run(), null,
                    Math.max(1, initialTicks), Math.max(1, periodTicks));
            return task::cancel;
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, work, initialTicks, periodTicks);
        return task::cancel;
    }

    public static void global(Plugin plugin, Runnable work) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, ignored -> work.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, work);
        }
    }

    public static Task asyncRepeating(Plugin plugin, Runnable work, long initialTicks, long periodTicks) {
        if (isFolia()) {
            var task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, ignored -> work.run(),
                    ticksToMillis(initialTicks), ticksToMillis(periodTicks), TimeUnit.MILLISECONDS);
            return task::cancel;
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, work, initialTicks, periodTicks);
        return task::cancel;
    }

    public static void async(Plugin plugin, Runnable work) {
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runNow(plugin, ignored -> work.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, work);
        }
    }

    private static long ticksToMillis(long ticks) {
        return Math.max(1L, ticks) * 50L;
    }
}
