package com.mrfuzzihead.framedcompactdrawers.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.jaquadro.minecraft.storagedrawers.client.renderer.ModularBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.client.renderer.PanelBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelper;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelperState;
import com.mrfuzzihead.framedcompactdrawers.FramedCompactDrawers;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedController;
import com.mrfuzzihead.framedcompactdrawers.block.tile.TileFramedController;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FramedControllerRenderer implements ISimpleBlockRenderingHandler {

    private final PanelBoxRenderer panelRenderer = new PanelBoxRenderer();
    private final ModularBoxRenderer invBoxRenderer = new ModularBoxRenderer();

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockFramedController)) return;
        BlockFramedController controller = (BlockFramedController) block;

        IIcon icon = controller.getDefaultFaceIcon();

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
        if (!(block instanceof BlockFramedController)) return false;
        BlockFramedController controller = (BlockFramedController) block;
        TileFramedController tile = controller.getTileEntityFramed(world, x, y, z);
        if (tile == null) return false;

        ItemStack matSide = tile.getEffectiveMaterialSide();
        ItemStack matFront = tile.getEffectiveMaterialFront();
        ItemStack matTrim = tile.getEffectiveMaterialTrim();

        IIcon sideIcon = resolveIcon(matSide, controller.getDefaultFaceIcon());
        IIcon trimIcon = resolveIcon(matTrim, controller.getDefaultTrimIcon());
        IIcon frontIcon = resolveIcon(matFront, controller.getDefaultFaceIcon());

        int side = tile.getDirection();
        double trimWidth = 0.0625;

        panelRenderer.setTrimWidth(trimWidth);
        panelRenderer.setTrimDepth(0);
        panelRenderer.setTrimColor(ModularBoxRenderer.COLOR_WHITE);
        panelRenderer.setPanelColor(ModularBoxRenderer.COLOR_WHITE);

        RenderHelper rh = RenderHelper.instances.get();
        rh.setColorAndBrightness(world, block, x, y, z);
        rh.state.setRotateTransform(RenderHelper.ZNEG, side);
        rh.state.setUVRotation(RenderHelper.YPOS, RenderHelperState.ROTATION_BY_FACE_FACE[RenderHelper.ZNEG][side]);

        panelRenderer.setTrimIcon(trimIcon);
        panelRenderer.setPanelIcon(sideIcon);
        for (int i = 0; i < 6; i++) {
            if (i != RenderHelper.ZNEG) {
                panelRenderer.renderFacePanel(i, world, controller, x, y, z, 0, 0, 0, 1, 1, 1);
            }
            panelRenderer.renderFaceTrim(i, world, controller, x, y, z, 0, 0, 0, 1, 1, 1);
        }

        panelRenderer.setTrimDepth(0.0625);
        panelRenderer.renderInteriorTrim(RenderHelper.ZNEG, world, controller, x, y, z, 0, 0, 0, 1, 1, 1);

        rh.state.flipTexture = true;
        rh.setRenderBounds(trimWidth, trimWidth, 0.0625, 1 - trimWidth, 1 - trimWidth, 1);
        rh.renderFace(RenderHelper.ZNEG, world, controller, x, y, z, frontIcon);
        rh.state.flipTexture = false;

        rh.state.clearRotateTransform();
        rh.state.clearUVRotation(RenderHelper.YPOS);
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
        return FramedCompactDrawers.proxy.framedControllerRenderID;
    }
}
