package com.jusipat.ndw;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBed;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;

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
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.block instanceof BlockBed) {
            //int bedDirection = event.block.getBedDirection(event.world, event.x, event.y, event.z);
            for (EntityPlayer victim : event.world.playerEntities) {
                ChunkCoordinates playerBedLoc = victim.getBedLocation(victim.dimension);
                if (playerBedLoc == null) { continue; }
                if ((playerBedLoc.posX == event.x || playerBedLoc.posX == event.x - 1 || playerBedLoc.posX == event.x + 1) // account for bed rotation, this sucks
                    && playerBedLoc.posY == event.y
                    && (playerBedLoc.posZ == event.z) || playerBedLoc.posZ == event.z - 1 || playerBedLoc.posZ == event.z + 1) {
                    UUID victimId = victim.getUniqueID();
                    bedDestroyedMap.put(victimId, true);
                    return;
                }
            }
        }
    }

    // todo: reset bedDestroyedMap entry for when a new spawn point is set

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
