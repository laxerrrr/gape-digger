package xyz.laxerrrr.diggermender;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class DiggerFMLEvents { //TODO: Make more consistent, robust -- might need a mixin

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Digger.digging = false;
        if (DiggerEvents.mongoClient != null) DiggerEvents.mongoClient.close(); //Close the MongoDB client
        DiggerEvents.connected = false;
        System.out.println("Logged out!");
    }
}
