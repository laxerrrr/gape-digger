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
    public static BetterBlockPos coord;
    public static int rowAmount = 0;
    public static int stage = 0;
    public static int currentLayer;
    public static boolean connected = false;
    public static MongoClient mongoClient;
    public static MongoCollection<Document> collection;
    public static MongoCollection<Document> partitionsCollection;
    public static MongoDatabase database;
    public static MongoDatabase partitionsDatabase;
    public static Document doc;
    public static Document partitions;
    public static Scanner s;
    public static ArrayList<Integer> allocatedRange;
    public static int currentPartition;
    public static boolean areacleared = false;
    public static long TICKS = 0;
    int playerID;
    UUID playerUUID;
    File file = new File(Minecraft.getMinecraft().gameDir.toString() + "/digger-mender");


    // @SubscribeEvent
   // public void playerJoinWorldEvent(EntityJoinWorldEvent event) {
        //Minecraft.getMinecraft().player.sendChatMessage("joined");
	/*

	*/
   // }


    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event) throws FileNotFoundException { //TODO: ON LOGOUT CANCEL
        if (event.phase == TickEvent.Phase.END) {
            TICKS++;
            if (TICKS % 40 == 0) {
                    if (!BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isActive() && !BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().isActive() && Digger.digging) {
                        System.out.println("bapi && bapi && digging");
                        if (!connected) {
                            System.out.println("Connecting");
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§4Connecting!"));
                            Scanner databaseIP = new Scanner(new File(Minecraft.getMinecraft().gameDir.toString() + "/Digger/databaseIP"));
                            UUID playerUUID = Minecraft.getMinecraft().player.getUniqueID();
                            System.out.println("start of connection");
                            String ip = databaseIP.next();
                            MongoClientURI uri = new MongoClientURI("mongodb://" + databaseIP.next() + ":" + databaseIP.next() + "@" + ip + "/?authSource=admin");
                            mongoClient = new MongoClient(uri);
                            database = mongoClient.getDatabase("digging-operation");
                            partitionsDatabase = mongoClient.getDatabase("partitions");
                            partitionsCollection = partitionsDatabase.getCollection("partitions");
                            collection = database.getCollection("bots-progress");
                            partitions = partitionsCollection.find().first();
                            try {
                                s = new Scanner(new File(Minecraft.getMinecraft().gameDir + "/Digger/partitions"));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            connected = true;
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§aConnected!"));
                        }
                        switch (stage) {
                            case 0: //ONLY FOR INITIAL MONGO LOGIC
                                if (doc == null) {
                                    ArrayList<Integer> tempparts = new ArrayList<Integer>();
                                    tempparts.add(s.nextInt());
                                    tempparts.add(s.nextInt());
                                    Document tempdoc = new Document("UUID", playerUUID)
                                            .append("currentpartition", tempparts.get(0))
                                            .append("currentlayer", 72);
                                    tempdoc.append("allocated", tempparts);
                                    collection.insertOne(tempdoc);
                                }
                                stage = 1;
                                break;
                            case 1: //Set correct coordinates and clear area
                                doc = collection.find(eq("UUID", playerUUID)).first();
                                allocatedRange = (ArrayList<Integer>) doc.get("allocated");
                                currentPartition = collection.find(eq("UUID", playerUUID)).first().getInteger("currentpartition");
                                currentLayer = collection.find(eq("UUID", playerUUID)).first().getInteger("currentlayer"); //Get current layer from database TODO: only check when new layer
                                ArrayList<Integer> xzcorner = new ArrayList<Integer>(); //Get the X and Z corner block coordinates for the partition
                                xzcorner.clear();
                                xzcorner = (ArrayList<Integer>) partitions.get(Integer.toString(currentPartition));
                                if (currentPartition > allocatedRange.get(1)) {
                                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("wat"));
                                }
                                if (!areacleared) {
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
                            case 4: //TODO: remove extra cases
                                stage = 5;
                                break;
                            case 5:
                                stage = 6;
                                break;
                            case 6:
                                stage = 7;
                                break;
                            case 7:
                                stage = 8;
                                break;
                            case 8:
                                if (DiggerEvents.rowAmount == 64) {
                                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§bDone layer"));
                                    rowAmount = 0;
                                    currentLayer -= 2;
                                    if (currentLayer <= 30) {
                                        collection.updateOne(eq("UUID", playerUUID), Updates.set("currentpartition", (currentPartition + 1)));
                                        currentLayer = 72;
                                        areacleared = false;
                                    }
                                    collection.updateOne(eq("UUID", playerUUID), Updates.set("currentlayer", currentLayer));
                                }
                                stage = 1;
                                break;
                            case 9:
                                stage = 1; //TEMP
                        }
                    }
                }
            }
        }
    }
