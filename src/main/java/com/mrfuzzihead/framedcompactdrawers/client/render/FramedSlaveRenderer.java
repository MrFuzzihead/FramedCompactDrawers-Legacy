package com.mrfuzzihead.framedcompactdrawers.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import com.jaquadro.minecraft.storagedrawers.client.renderer.ModularBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.client.renderer.PanelBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelper;
import com.mrfuzzihead.framedcompactdrawers.FramedCompactDrawers;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedSlave;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedSlave;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FramedSlaveRenderer implements ISimpleBlockRenderingHandler {

    private final PanelBoxRenderer panelRenderer = new PanelBoxRenderer();
    private final ModularBoxRenderer invBoxRenderer = new ModularBoxRenderer();
    private final FallbackBoxRenderer fallbackRenderer = new FallbackBoxRenderer();

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockFramedSlave)) return;
        BlockFramedSlave slave = (BlockFramedSlave) block;

        IIcon icon = slave.getDefaultFaceIcon();

        GL11.glPushMatrix();
        GL11.glRotatef(90, 0, 1, 0);
        GL11.glTranslatef(-0.5f, -0.5f, -0.5f);

        invBoxRenderer.setUnit(0.0625);
        invBoxRenderer.setColor(ModularBoxRenderer.COLOR_WHITE);
        invBoxRenderer.setIcon(icon);

        invBoxRenderer.renderSolidBox(null, block, 0, 0, 0, 0, 0, 0, 1, 1, 1);

        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (!(block instanceof BlockFramedSlave)) return false;
        BlockFramedSlave slave = (BlockFramedSlave) block;
        com.jaquadro.minecraft.storagedrawers.block.tile.TileEntitySlave te = slave.getTileEntity(world, x, y, z);

        if (!(te instanceof TileFramedSlave)) {
            fallbackRenderer.render(
                world,
                block,
                x,
                y,
                z,
                slave.getDefaultFaceIcon(),
                slave.getDefaultTrimIcon(),
                FallbackBoxRenderer.DEFAULT_DIRECTION);
            return true;
        }

        TileFramedSlave tile = (TileFramedSlave) te;

        ItemStack matSide = tile.getEffectiveMaterialSide();
        ItemStack matTrim = tile.getEffectiveMaterialTrim();
        ItemStack matTopBottom = tile.getEffectiveMaterialFront();

        IIcon sideIcon = resolveIcon(matSide, slave.getDefaultFaceIcon());
        IIcon trimIcon = resolveIcon(matTrim, slave.getDefaultTrimIcon());
        IIcon topBottomIcon = resolveIcon(matTopBottom, slave.getDefaultTopBottomIcon());

        double trimWidth = 0.0625;
        panelRenderer.setTrimWidth(trimWidth);
        panelRenderer.setTrimDepth(0);
        panelRenderer.setTrimColor(ModularBoxRenderer.COLOR_WHITE);
        panelRenderer.setPanelColor(ModularBoxRenderer.COLOR_WHITE);

        RenderHelper rh = RenderHelper.instances.get();
        rh.setColorAndBrightness(world, block, x, y, z);

        // Protect against stale state from previous renderers on this thread. clearRotateTransform() also
        // resets RenderHelperState.tilt and the orientation matrices, so those are restored here too even
        // though nothing in this renderer sets them yet.
        int prevRotate = rh.state.rotateTransform;
        int prevTilt = rh.state.tilt;
        int prevUvRotY = rh.state.uvRotate[RenderHelper.YPOS];
        int prevUvRotN = rh.state.uvRotate[RenderHelper.YNEG];

        int pass = ForgeHooksClient.getWorldRenderPass();

        if (pass == 0) {
            panelRenderer.setTrimIcon(trimIcon);
            for (int i = 0; i < 6; i++) {
                panelRenderer.setPanelIcon(i == 0 || i == 1 ? topBottomIcon : sideIcon);
                panelRenderer.renderFacePanel(i, world, slave, x, y, z, 0, 0, 0, 1, 1, 1);
                panelRenderer.renderFaceTrim(i, world, slave, x, y, z, 0, 0, 0, 1, 1, 1);
            }
        } else if (pass == 1) {
            IIcon sideShadow = slave.getOverlaySideShadow();
            if (sideShadow != null) {
                GL11.glDepthMask(false);
                try {
                    panelRenderer.setTrimIcon(sideShadow);
                    for (int i = 2; i < 6; i++) {
                        panelRenderer.setPanelIcon(sideShadow);
                        panelRenderer.renderFacePanel(i, world, slave, x, y, z, 0, 0, 0, 1, 1, 1);
                        panelRenderer.renderFaceTrim(i, world, slave, x, y, z, 0, 0, 0, 1, 1, 1);
                    }
                } finally {
                    GL11.glDepthMask(true);
                }
            }
        }

        // Restore previous state
        rh.state.rotateTransform = prevRotate;
        rh.state.tilt = prevTilt;
        rh.state.uvRotate[RenderHelper.YPOS] = prevUvRotY;
        rh.state.uvRotate[RenderHelper.YNEG] = prevUvRotN;
        return true;
    }

    private IIcon resolveIcon(ItemStack stack, IIcon fallback) {
        if (stack == null || stack.getItem() == null) return fallback;
        Block b = Block.getBlockFromItem(stack.getItem());
        if (b == null) return fallback;
        IIcon icon = b.getIcon(4, stack.getItemDamage());
        return icon != null ? icon : fallback;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return FramedCompactDrawers.proxy.framedSlaveRenderID;
    }
}
