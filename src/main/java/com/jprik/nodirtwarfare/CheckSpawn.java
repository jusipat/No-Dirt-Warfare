package com.jprik.nodirtwarfare;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class CheckSpawn {
    @Mod.EventBusSubscriber(modid = NoDirtWarfare.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (event.getTarget() instanceof ServerPlayer && event.getEntity() instanceof ServerPlayer) {
                ServerPlayer attacker = (ServerPlayer) event.getEntity();

                // Check if the attacker has a respawn point set
                if (attacker.getRespawnPosition() == null) {
                    event.setCanceled(true);

                }
            }
        }
    }
}

