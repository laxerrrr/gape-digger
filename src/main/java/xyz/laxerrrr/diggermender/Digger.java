
package xyz.laxerrrr.diggermender;

import baritone.api.*;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.bson.Document;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
public class Digger {
    public static boolean digging = false;
    public static void startDig() throws InterruptedException {

        digging = true;
    }
    public static void stopDig(){
        //TODO: Make Baritone cancel or pause
        digging = false;
    }
    public static void cancel(){
        digging = false;
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("cancel");
        DiggerEvents.stage = 0;
    }

    public static void rowDig(BetterBlockPos coord) { //1
        BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().clearArea(coord, coord.north(63).down(1));
    }

    public static void clearTop(BetterBlockPos coord) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().clearArea(coord, coord.north(63).west(63).up(20));
    }
    
    public static void cleanPart(BetterBlockPos coord){
        BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().clearArea(coord, coord.north(63).west(63).down(42));
    }

    public static void connect(){
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("** LITTLE EASTER EGG YOU FUCKER WOOHOO **"));
    }
}
