package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.HealthTracker;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 客户端 tick 事件入口。
 */
public final class ClientTickEvents {
    private ClientTickEvents() {
    }

    /**
     * 在客户端 tick 结束时刷新生命值缓存与浮动数字。
     *
     * @param event 客户端 tick 事件
     */
    public static void onClientTick(ClientTickEvent.Post event) {
        HealthTracker.tick();
    }
}
