//HUGE MESS, needs to be refactored...

package xyz.laxerrrr.diggermender;

import baritone.api.BaritoneAPI;
import baritone.api.utils.BetterBlockPos;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class DiggerEvents {

    BetterBlockPos coord;
    int rowAmount = 0;
    public static int stage = 0;
    int currentLayer;

    public static boolean connected = false;
    public static MongoClient mongoClient;
    MongoCollection<Document> collection;
    MongoCollection<Document> partitionsCollection;
    MongoDatabase database;
    MongoDatabase partitionsDatabase;
    Document doc;
    Document partitions;

    Scanner s;
    ArrayList<Integer> allocatedRange;
    int currentPartition;

    boolean areacleared = false;
    long TICKS = 0;
    boolean inGame = false;
    UUID playerUUID;
    File file = new File(Minecraft.getMinecraft().gameDir.toString() + "/digger-mender");


    @SubscribeEvent
    public void playerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        System.out.println("Logged in");
        inGame = true;
   }

   public void playerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event){
        inGame = false;
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("cancel");
        mongoClient.close();
   }


    @SubscribeEvent //TODO: Fix whitespace
    public void tickEvent(TickEvent.ClientTickEvent event) throws FileNotFoundException { //Fires every start and end of Client Tick
        if (event.phase == TickEvent.Phase.END) { //Fires at end of tick
            TICKS++; //Counts ticks
            if (TICKS % 40 == 0) { //If ticks are divisible by 40, then go on
                if (inGame) {
                    if (!connected) { //Initial connection logic
                        System.out.println("Connecting");
                        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§4Connecting!"));
                        Scanner databaseIP = new Scanner(new File(Minecraft.getMinecraft().gameDir.toString() + "/Digger/databaseIP"));
                        UUID playerUUID = Minecraft.getMinecraft().player.getUniqueID();
                        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(playerUUID.toString()));
                        System.out.println("start of connection");
                        String ip = databaseIP.next();
                        MongoClientURI uri = new MongoClientURI("mongodb://" + databaseIP.next() + ":" + databaseIP.next() + "@" + ip + "/?authSource=admin");
                        mongoClient = new MongoClient(uri);
                        database = mongoClient.getDatabase("digging-operation");
                        partitionsDatabase = mongoClient.getDatabase("partitions");
                        partitionsCollection = partitionsDatabase.getCollection("partitions");
                        collection = database.getCollection("bots-progress");
                        partitions = partitionsCollection.find().first(); //Partitions list
                        doc = collection.find(eq("UUID", playerUUID)).first();
                        try {
                            s = new Scanner(new File(Minecraft.getMinecraft().gameDir + "/Digger/partitions"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        connected = true;
                        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§aConnected!"));
                    }
                    if (!BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isActive() && !BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive() && Digger.digging) { //Checks if Baritone is not busy and if the bot has to be active
                        switch (stage) {
                            case 0: //Only fired on first run through
                                if (doc == null) { //If mongo can't find the bot's progress document in the database, then create one
                                    ArrayList<Integer> tempparts = new ArrayList<Integer>();
                                    tempparts.add(s.nextInt());
                                    tempparts.add(s.nextInt());
                                    Document tempdoc = new Document("UUID", playerUUID)
                                            .append("currentpartition", tempparts.get(0))
                                            .append("currentlayer", 72);
                                    tempdoc.append("allocated", tempparts);
                                    collection.insertOne(tempdoc);
                                    doc = tempdoc;
                                }
                                stage = 1;
                                break;
                            case 1: //Set correct coordinates and clear area
                                allocatedRange = (ArrayList<Integer>) doc.get("allocated");
                                currentPartition = collection.find(eq("UUID", playerUUID)).first().getInteger("currentpartition");
                                currentLayer = collection.find(eq("UUID", playerUUID)).first().getInteger("currentlayer"); //Get current layer from database TODO: only check when new layer
                                ArrayList<Integer> xzcorner = new ArrayList<Integer>(); //Get the X and Z corner block coordinates for the partition
                                xzcorner = (ArrayList<Integer>) partitions.get(Integer.toString(currentPartition));
                                if (currentPartition > allocatedRange.get(1)) {
                                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("wat")); //TODO: Add logic here for when bot goes over allocated range of partitions
                                }
                                if (!areacleared) { //Clears top of partition before row-digging logic
                                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§bClearing top!"));
                                    coord = new BetterBlockPos(xzcorner.get(0), 72, xzcorner.get(1)); //Set the starting row coordinate
                                    Digger.clearTop(coord);
                                }
                                coord = new BetterBlockPos(xzcorner.get(0) - rowAmount, currentLayer, xzcorner.get(1)); //Set the starting row coordinate
                                stage = 2;
                                break;
                            case 2: //Dig row
                                areacleared = true;
                                Digger.rowDig(coord);
                                stage = 3;
                                break;
                            case 3: //After digging row is done, add 1 to the amount of rows
                                rowAmount++;
                                stage = 4;
                                break;
                            case 4:
                                if (rowAmount == 64) {
                                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§bDone layer"));
                                    rowAmount = 0;
                                    currentLayer -= 2;
                                    if (currentLayer <= 30) { //Update database when all layers of partition are done
                                        collection.updateOne(eq("UUID", playerUUID), Updates.set("currentpartition", (currentPartition + 1)));
                                        currentLayer = 72;
                                        areacleared = false;
                                    }
                                    collection.updateOne(eq("UUID", playerUUID), Updates.set("currentlayer", currentLayer)); //Always update layer #
                                }
                                stage = 1;
                                break;
                        }
                    }
                }
                }
            }
        }
    }
