package xyz.laxerrrr.diggermender;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.client.ClientCommandHandler;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

@Mod(modid = DiggerMender.MODID, name = DiggerMender.NAME, version = DiggerMender.VERSION)
public class DiggerMender
{
    public static final String MODID = "diggermender";
    public static final String NAME = "Digger Mender";
    public static final String VERSION = "1.0";


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
	ClientCommandHandler.instance.registerCommand(new CommandStartDig());
	ClientCommandHandler.instance.registerCommand(new CommandStopDig());
    ClientCommandHandler.instance.registerCommand(new CommandCancelDig());
    ClientCommandHandler.instance.registerCommand(new CommandConnect());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new DiggerEvents());
        MinecraftForge.EVENT_BUS.register(DiggerFMLEvents.class);
    }
}
