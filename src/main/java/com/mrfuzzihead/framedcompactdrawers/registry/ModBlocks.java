package com.mrfuzzihead.framedcompactdrawers.registry;

import net.minecraft.block.Block;

import com.mrfuzzihead.framedcompactdrawers.FramedCompactDrawers;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedCompactDrawer;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedController;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedSlave;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedCompactDrawer;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedController;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedSlave;
import com.mrfuzzihead.framedcompactdrawers.item.ItemFramedCompactDrawer;
import com.mrfuzzihead.framedcompactdrawers.item.ItemFramedController;
import com.mrfuzzihead.framedcompactdrawers.item.ItemFramedSlave;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block blockFramedCompactDrawer = null;
    public static Block blockFramedController = null;
    public static Block blockFramedSlave = null;

    /**
     * Registry names passed to FML must be a bare path. {@code GameData.addPrefix} only substitutes the
     * active modid when the name contains no {@code ':'}, so passing "framedcompactdrawers.framed_slave"
     * produced the registry name {@code framedcompactdrawers:framedcompactdrawers.framed_slave}, which breaks
     * {@code /give} by ResourceLocation, {@code Block.getBlockFromResourceLocation}, and any integration
     * keyed on modid:path. The dotted form is still correct as the argument to the block constructors, which
     * feeds {@code setBlockName} and therefore the {@code tile.*} lang keys.
     *
     * <p>
     * The TileEntity ids below are deliberately left as they are. 1.7.10 persists that string into chunk NBT
     * via {@code TileEntity.writeToNBT} and resolves it back through {@code nameToClassMap} in
     * {@code createAndLoadEntity}, so renaming one would strip the TileEntity from every framed block already
     * placed in an existing world.
     */
    public static void register() {
        // Phase 2: Framed Compact Drawer
        blockFramedCompactDrawer = new BlockFramedCompactDrawer();
        GameRegistry.registerBlock(blockFramedCompactDrawer, ItemFramedCompactDrawer.class, "framed_compact_drawer");
        GameRegistry
            .registerTileEntity(TileFramedCompactDrawer.class, FramedCompactDrawers.MODID + ".framed_compact_drawer");

        // Phase 3: Framed Controller
        blockFramedController = new BlockFramedController();
        GameRegistry.registerBlock(blockFramedController, ItemFramedController.class, "framed_drawer_controller");
        GameRegistry.registerTileEntity(TileFramedController.class, FramedCompactDrawers.MODID + ".framed_controller");

        // Phase 4: Framed Slave
        blockFramedSlave = new BlockFramedSlave();
        GameRegistry.registerBlock(blockFramedSlave, ItemFramedSlave.class, "framed_slave");
        GameRegistry.registerTileEntity(TileFramedSlave.class, FramedCompactDrawers.MODID + ".framed_slave");
    }
}
