package com.mrfuzzihead.framedcompactdrawers.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.jaquadro.minecraft.storagedrawers.item.ItemCustomDrawers;
import com.jaquadro.minecraft.storagedrawers.util.DrawerOrientation;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedController;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFramedController extends ItemCustomDrawers {

    public ItemFramedController(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        list.add(StatCollector.translateToLocalFormatted("storageDrawers.controller.description"));
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        if (!world.setBlock(x, y, z, field_150939_a, metadata, 3)) {
            return false;
        }

        TileFramedController tile = (TileFramedController) world.getTileEntity(x, y, z);

        if (world.getBlock(x, y, z) == field_150939_a) {
            // Since StorageDrawers 2.2.28 the block no longer derives its facing during onBlockPlacedBy; the
            // item block owns orientation now. Resolve it the same way ItemDrawers.placeBlockAt does, or the
            // tile keeps direction 0 (the down face), which makes the controller inert and paints the front
            // panel on the bottom of the block.
            if (tile != null) {
                DrawerOrientation orientation = DrawerOrientation.forEntity(player);
                tile.setDirection(orientation.direction());
                tile.setRotation(orientation.rotation());
            }

            field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack);
            field_150939_a.onPostBlockPlaced(world, x, y, z, metadata);

            if (world.isRemote) world.markBlockForUpdate(x, y, z);
        }

        if (tile != null && stack.hasTagCompound()
            && !stack.getTagCompound()
                .hasKey("tile")) {
            if (stack.getTagCompound()
                .hasKey("MatS"))
                tile.setMaterialSide(
                    ItemStack.loadItemStackFromNBT(
                        stack.getTagCompound()
                            .getCompoundTag("MatS")));
            if (stack.getTagCompound()
                .hasKey("MatT"))
                tile.setMaterialTrim(
                    ItemStack.loadItemStackFromNBT(
                        stack.getTagCompound()
                            .getCompoundTag("MatT")));
            if (stack.getTagCompound()
                .hasKey("MatF"))
                tile.setMaterialFront(
                    ItemStack.loadItemStackFromNBT(
                        stack.getTagCompound()
                            .getCompoundTag("MatF")));
        }

        return true;
    }

    /**
     * {@code ItemDrawers} only uses these to size the tooltip, so the raw fields are the right source here. The
     * placed tile is written back with whatever is actually set, which is why a block that never went through
     * the framing table drops as a material-less stack and re-renders with the raw default icons.
     */
    public ItemStack getStack(int metadata) {
        return ItemCustomDrawers.makeItemStack(field_150939_a, 1, null, null, null);
    }

    /** Build a drop ItemStack that preserves the tile's material data. */
    public ItemStack makeDropStack(TileFramedController tile) {
        return ItemCustomDrawers
            .makeItemStack(field_150939_a, 1, tile.getMaterialSide(), tile.getMaterialTrim(), tile.getMaterialFront());
    }
}
