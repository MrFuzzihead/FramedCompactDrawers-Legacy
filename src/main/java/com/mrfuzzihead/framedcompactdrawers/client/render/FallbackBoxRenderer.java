package com.mrfuzzihead.framedcompactdrawers.client.render;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import com.jaquadro.minecraft.storagedrawers.client.renderer.ModularBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.client.renderer.PanelBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelper;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelperState;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Draws a plain framed box for a block whose TileEntity is not available, so the block degrades to a solid
 * placeholder instead of disappearing.
 *
 * <p>
 * Every world renderer in this mod looks its TileEntity up through the {@code IBlockAccess} that
 * {@code WorldRenderer.updateRenderer} supplies, which is a {@code ChunkCache}. That returns null whenever the
 * chunk is outside the renderer's cache, and in the window between a block being placed on the server and the
 * client receiving its TileEntity. Returning false from {@code renderWorldBlock} in that situation makes
 * {@code RenderBlocks} draw nothing at all, and because these blocks are non-opaque the surrounding blocks
 * still draw their faces toward the hole, so the player sees a gap in the world.
 *
 * <p>
 * Storage Drawers avoids this by never bailing: {@code BlockController.getIcon(IBlockAccess, ...)} falls back to
 * {@code iconFront} when the tile is null, and {@code BlockDrawers.getIcon(...)} falls back to
 * {@code iconFront[0]}. This is the same idea for the framed blocks, using the raw default icons.
 */
@SideOnly(Side.CLIENT)
final class FallbackBoxRenderer {

    private static final double TRIM_WIDTH = 0.0625;

    /**
     * The default facing used when there is no TileEntity to read a direction from. Matches the vanilla
     * Drawer Controller, which faces north out of the box.
     */
    static final int DEFAULT_DIRECTION = 4;

    private final PanelBoxRenderer panelRenderer = new PanelBoxRenderer();

    void render(IBlockAccess world, Block block, int x, int y, int z, IIcon faceIcon, IIcon trimIcon, int direction) {
        // Only the opaque pass. The block body is not depth-masked here, so drawing it in the transparent pass
        // would fight with whatever the pass is compositing.
        if (ForgeHooksClient.getWorldRenderPass() != 0) return;

        panelRenderer.setTrimWidth(TRIM_WIDTH);
        panelRenderer.setTrimDepth(0);
        panelRenderer.setTrimColor(ModularBoxRenderer.COLOR_WHITE);
        panelRenderer.setPanelColor(ModularBoxRenderer.COLOR_WHITE);
        panelRenderer.setTrimIcon(trimIcon);
        panelRenderer.setPanelIcon(faceIcon);

        RenderHelper rh = RenderHelper.instances.get();
        rh.setColorAndBrightness(world, block, x, y, z);
        rh.state.setRotateTransform(RenderHelper.ZNEG, direction);
        rh.state
            .setUVRotation(RenderHelper.YPOS, RenderHelperState.ROTATION_BY_FACE_FACE[RenderHelper.ZNEG][direction]);

        try {
            for (int i = 0; i < 6; i++) {
                panelRenderer.renderFacePanel(i, world, block, x, y, z, 0, 0, 0, 1, 1, 1);
                panelRenderer.renderFaceTrim(i, world, block, x, y, z, 0, 0, 0, 1, 1, 1);
            }
        } finally {
            // The shared RenderHelperState is a thread local, so leaving a transform behind would corrupt the
            // next renderer to run on this thread.
            rh.state.clearRotateTransform();
            rh.state.clearUVRotation(RenderHelper.YPOS);
        }
    }
}
