package com.jprik.nodirtwarfare;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class CheckSpawn {
    @Mod.EventBusSubscriber(modid = NoDirtWarfare.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void checkSpawnPoint(PlayerEvent event) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            if (player.getRespawnPosition() == event.getEntity().getLevel().getSharedSpawnPos()) {
                
            }
        }
    }
}

