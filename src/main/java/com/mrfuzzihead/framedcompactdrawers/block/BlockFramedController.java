package com.mrfuzzihead.framedcompactdrawers.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.security.ISecurityProvider;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.BlockController;
import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import com.jaquadro.minecraft.storagedrawers.item.ItemCustomDrawers;
import com.jaquadro.minecraft.storagedrawers.item.ItemPersonalKey;
import com.mrfuzzihead.framedcompactdrawers.FramedCompactDrawers;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedController;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFramedController extends BlockController {

    @SideOnly(Side.CLIENT)
    private IIcon overlayHandle;

    @SideOnly(Side.CLIENT)
    private IIcon overlayFaceShadow;

    @SideOnly(Side.CLIENT)
    private IIcon overlayTrimShadow;

    @SideOnly(Side.CLIENT)
    private IIcon overlayTrimFace;

    @SideOnly(Side.CLIENT)
    private IIcon defaultFace;

    @SideOnly(Side.CLIENT)
    private IIcon defaultTrim;

    public BlockFramedController() {
        super(FramedCompactDrawers.MODID + ".framed_drawer_controller");
        setStepSound(Block.soundTypeWood);
        setHarvestLevel("axe", 0);
    }

    @Override
    public int getRenderType() {
        return FramedCompactDrawers.proxy.framedControllerRenderID;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean canRenderInPass(int pass) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileFramedController te = getTileEntitySafeFramed(world, x, y, z);
        if (te == null) return false;

        if (side != te.getDirection()) {
            return false;
        }

        ItemStack item = player.inventory.getCurrentItem();
        if (item != null && item.getItem() != null) {
            if (item.getItem() == ModItems.upgradeLock) {
                if (!world.isRemote) {
                    te.toggleLock(
                        java.util.EnumSet.allOf(LockAttribute.class),
                        LockAttribute.LOCK_POPULATED,
                        player.getGameProfile());
                }
                return true;
            } else if (item.getItem() == ModItems.shroudKey) {
                if (!world.isRemote) te.toggleShroud(player.getGameProfile());
                return true;
            } else if (item.getItem() == ModItems.quantifyKey) {
                if (!world.isRemote) te.toggleQuantify(player.getGameProfile());
                return true;
            } else if (item.getItem() instanceof ItemPersonalKey) {
                String securityKey = ModItems.personalKey.getSecurityProviderKey(0);
                ISecurityProvider provider = StorageDrawers.securityRegistry.getProvider(securityKey);
                if (!world.isRemote) te.toggleProtection(player.getGameProfile(), provider);
                return true;
            }
        }

        if (!world.isRemote) te.interactPutItemsIntoInventory(player);

        return true;
    }

    @Override
    public TileFramedController createNewTileEntity(World world, int meta) {
        return new TileFramedController();
    }

    public TileFramedController getTileEntityFramed(IBlockAccess blockAccess, int x, int y, int z) {
        net.minecraft.tileentity.TileEntity tile = blockAccess.getTileEntity(x, y, z);
        return (tile instanceof TileFramedController) ? (TileFramedController) tile : null;
    }

    public TileFramedController getTileEntitySafeFramed(World world, int x, int y, int z) {
        TileFramedController tile = getTileEntityFramed(world, x, y, z);
        if (tile == null) {
            tile = new TileFramedController();
            tile.xCoord = x;
            tile.yCoord = y;
            tile.zCoord = z;
            tile.setWorldObj(world);
            world.setTileEntity(x, y, z, tile);
        }
        return tile;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (willHarvest) return true;
        return super.removedByPlayer(world, player, x, y, z, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        super.harvestBlock(world, player, x, y, z, meta);
        world.setBlockToAir(x, y, z);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        TileFramedController tile = getTileEntityFramed(world, x, y, z);
        if (tile != null) {
            drops.add(
                ItemCustomDrawers
                    .makeItemStack(this, 1, tile.getMaterialSide(), tile.getMaterialTrim(), tile.getMaterialFront()));
        } else {
            drops.add(new ItemStack(Item.getItemFromBlock(this), 1, metadata));
        }
        return drops;
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        super.registerBlockIcons(register);

        overlayHandle = register.registerIcon(FramedCompactDrawers.MODID + ":overlay/handle");
        overlayFaceShadow = register.registerIcon(FramedCompactDrawers.MODID + ":overlay/shading_controller_face");
        overlayTrimShadow = register.registerIcon(FramedCompactDrawers.MODID + ":overlay/shading_controller_trim");
        overlayTrimFace = register.registerIcon(FramedCompactDrawers.MODID + ":overlay/shading_controller_bold_trim");

        defaultFace = register.registerIcon(FramedCompactDrawers.MODID + ":raw_side");
        defaultTrim = register.registerIcon(FramedCompactDrawers.MODID + ":raw_side");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getHandleOverlay() {
        return overlayHandle;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getFaceShadowOverlay() {
        return overlayFaceShadow;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getTrimShadowOverlay(boolean strong) {
        if (strong) return overlayTrimFace;
        else return overlayTrimShadow;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getDefaultFaceIcon() {
        return defaultFace;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getDefaultTrimIcon() {
        return defaultTrim;
    }
}
