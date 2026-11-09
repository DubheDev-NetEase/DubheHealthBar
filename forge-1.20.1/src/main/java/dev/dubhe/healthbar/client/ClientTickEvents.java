package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.DubheHealthBar;
import dev.dubhe.healthbar.HealthTracker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端 tick 事件入口。
 */
@Mod.EventBusSubscriber(modid = DubheHealthBar.MOD_ID, value = Dist.CLIENT)
public final class ClientTickEvents {
    private ClientTickEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HealthTracker.tick();
        }
    }
}
