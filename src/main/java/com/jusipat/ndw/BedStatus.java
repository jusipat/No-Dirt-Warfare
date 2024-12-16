package com.jusipat.ndw;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BedStatus {

    private static final Map<UUID, Boolean> bedDestroyedMap = new HashMap<>();

    private static final Map<UUID, Long> effectTimers = new HashMap<>();

    private static final long EFFECT_DURATION = 30 * 60 * 1000;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new BedStatus());
        System.err.println("Registered NDW BedStatus checker");
    }

    public static void setBedDestroyed(UUID playerUUID) {
        bedDestroyedMap.put(playerUUID, true);
    }

    public static boolean isBedDestroyed(UUID playerUUID) {
        return bedDestroyedMap.getOrDefault(playerUUID, false);
    }

    public static void resetBedStatus(UUID playerUUID) {
        bedDestroyedMap.put(playerUUID, false);
    }

    @SubscribeEvent
    public void onBlockBreak(net.minecraftforge.event.world.BlockEvent.BreakEvent event) {
        if (event.block instanceof net.minecraft.block.BlockBed) {
            int x = event.x;
            int y = event.y;
            int z = event.z;
            MyMod.LOG.info("Bed X: " + x + "Bed Y: " + y + "Bed Z: " + y);

            for (Object playerObj : event.world.playerEntities) { // todo: fix this (not working at least in LAN tests)
                if (playerObj instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) playerObj;
                    if (player.getBedLocation() != null &&
                        player.getBedLocation().equals(new ChunkCoordinates(x, y, z))) {
                        UUID playerUUID = player.getUniqueID();
                        bedDestroyedMap.put(playerUUID, true);
                        MyMod.LOG.info("Bed destroyed for player: " + player.getDisplayName());
                        return;
                    }
                }
            }
            MyMod.LOG.info("A bed was destroyed, but no linked player was found.");
        }
    }


    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.entity instanceof EntityPlayer) {
            EntityPlayer victim = (EntityPlayer) event.entity;
            if (bedDestroyedMap.getOrDefault(victim.getUniqueID(), false)) {
                effectTimers.put(victim.getUniqueID(), System.currentTimeMillis() + EFFECT_DURATION);
                MyMod.LOG.info("Player " + victim.getDisplayName() + " will have effects applied on respawn.");
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        UUID playerUUID = player.getUniqueID();
        if (effectTimers.containsKey(playerUUID)) {
            long expirationTime = effectTimers.get(playerUUID);
            long currentTime = System.currentTimeMillis();
            if (currentTime < expirationTime) {
                applyEffects(player);
                MyMod.LOG.info("Applied effects to player: " + player.getDisplayName());
            } else {
                effectTimers.remove(playerUUID);
                MyMod.LOG.info("Effects expired for player: " + player.getDisplayName());
            }
        }
    }

    private void applyEffects(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(Potion.weakness.id, Integer.MAX_VALUE, 1, true));
        player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, Integer.MAX_VALUE, 1, true));
        player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, Integer.MAX_VALUE, 1, true));
    }
}
