package com.mrfuzzihead.framedcompactdrawers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = FramedCompactDrawers.MODID,
    version = Tags.VERSION,
    name = FramedCompactDrawers.MODNAME,
    dependencies = FramedCompactDrawers.DEPENDENCIES,
    acceptedMinecraftVersions = "[1.7.10]")
public class FramedCompactDrawers {

    public static final String MODID = "framedcompactdrawers";
    public static final String MODNAME = "Framed Compact Drawers";
    /**
     * StorageDrawers 2.2.28 reworked the drawer orientation model: {@code direction} stopped being derived in
     * {@code BlockController.onBlockPlacedBy} and moved into the item block via
     * {@code DrawerOrientation.forEntity}, and a second {@code rotation} field was added next to it. This mod
     * targets the new model and links against {@code DrawerOrientation} / {@code getRotation()}, so an older
     * StorageDrawers either refuses to load or silently places blocks with no facing at all. Pin the floor
     * instead of relying on the pack to stay behind.
     *
     * <p>UniMixins is a hard requirement because the Storage Drawers framing-table and item-renderer
     * integration is loaded through {@code com.gtnewhorizon.gtnhmixins.ILateMixinLoader}, which ships in
     * UniMixins rather than GTNHLib.
     */
    public static final String DEPENDENCIES = "required-after:StorageDrawers@[2.2.31,);required-after:unimixins;after:waila;";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.mrfuzzihead.framedcompactdrawers.ClientProxy",
        serverSide = "com.mrfuzzihead.framedcompactdrawers.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
