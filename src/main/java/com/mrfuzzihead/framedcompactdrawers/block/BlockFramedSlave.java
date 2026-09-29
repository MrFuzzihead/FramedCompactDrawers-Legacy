package com.mrfuzzihead.framedcompactdrawers.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.security.ISecurityProvider;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.BlockSlave;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;
import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import com.jaquadro.minecraft.storagedrawers.item.ItemCustomDrawers;
import com.jaquadro.minecraft.storagedrawers.item.ItemPersonalKey;
import com.mrfuzzihead.framedcompactdrawers.FramedCompactDrawers;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedSlave;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFramedSlave extends BlockSlave {

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

    @SideOnly(Side.CLIENT)
    private IIcon defaultTopBottom;

    @SideOnly(Side.CLIENT)
    private IIcon overlaySideShadow;

    public BlockFramedSlave() {
        super(FramedCompactDrawers.MODID + ".framed_slave");
        setStepSound(Block.soundTypeWood);
        setHarvestLevel("axe", 0);
    }

    @Override
    public int getRenderType() {
        return FramedCompactDrawers.proxy.framedSlaveRenderID;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
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
        TileFramedSlave slave = getTileEntitySafe(world, x, y, z);
        if (slave == null) return false;

        ItemStack item = player.inventory.getCurrentItem();
        if (item != null && item.getItem() != null) {
            if (item.getItem() == ModItems.upgradeLock) {
                if (!world.isRemote) {
                    TileEntityController controller = slave.getController();
                    if (controller != null) {
                        controller.toggleLock(
                            java.util.EnumSet.allOf(LockAttribute.class),
                            LockAttribute.LOCK_POPULATED,
                            player.getGameProfile());
                    }
                }
                return true;
            } else if (item.getItem() == ModItems.shroudKey) {
                if (!world.isRemote) {
                    TileEntityController controller = slave.getController();
                    if (controller != null) controller.toggleShroud(player.getGameProfile());
                }
                return true;
            } else if (item.getItem() == ModItems.quantifyKey) {
                if (!world.isRemote) {
                    TileEntityController controller = slave.getController();
                    if (controller != null) controller.toggleQuantify(player.getGameProfile());
                }
                return true;
            } else if (item.getItem() instanceof ItemPersonalKey) {
                if (!world.isRemote) {
                    TileEntityController controller = slave.getController();
                    if (controller != null) {
                        String securityKey = ModItems.personalKey.getSecurityProviderKey(0);
                        ISecurityProvider provider = StorageDrawers.securityRegistry.getProvider(securityKey);
                        controller.toggleProtection(player.getGameProfile(), provider);
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public TileFramedSlave createNewTileEntity(World world, int meta) {
        return new TileFramedSlave();
    }

    public TileFramedSlave getTileEntityFramed(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileFramedSlave ? (TileFramedSlave) tile : null;
    }

    public TileFramedSlave getTileEntitySafe(World world, int x, int y, int z) {
        TileFramedSlave tile = getTileEntityFramed(world, x, y, z);
        if (tile == null) {
            tile = new TileFramedSlave();
            tile.xCoord = x;
            tile.yCoord = y;
            tile.zCoord = z;
            tile.setWorldObj(world);
            world.setTileEntity(x, y, z, tile);
            // BlockSlave.getTileEntitySafe does this too. TileEntitySlave.ensureInitialized() populates
            // selfCoord, which getAccessibleDrawerSlots() hands to TileEntityController.isValidSlave(). Today
            // the only path that reaches that call goes through getController(), which initialises it anyway,
            // so this restores the parent's invariant rather than fixing an observed failure.
            tile.ensureInitialized();
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
        TileFramedSlave tile = getTileEntityFramed(world, x, y, z);
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
        overlaySideShadow = register.registerIcon(FramedCompactDrawers.MODID + ":overlay/shading_side");

        defaultFace = register.registerIcon(FramedCompactDrawers.MODID + ":raw_side");
        defaultTrim = register.registerIcon(FramedCompactDrawers.MODID + ":raw_side");
        defaultTopBottom = register.registerIcon(FramedCompactDrawers.MODID + ":slave_raw_top_bottom");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getDefaultTopBottomIcon() {
        return defaultTopBottom;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getOverlaySideShadow() {
        return overlaySideShadow;
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
