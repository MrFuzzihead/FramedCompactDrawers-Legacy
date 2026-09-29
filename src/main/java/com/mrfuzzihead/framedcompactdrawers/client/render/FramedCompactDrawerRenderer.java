package com.mrfuzzihead.framedcompactdrawers.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.client.renderer.DrawersRenderer;
import com.jaquadro.minecraft.storagedrawers.client.renderer.ModularBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.client.renderer.PanelBoxRenderer;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelper;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelperState;
import com.mrfuzzihead.framedcompactdrawers.FramedCompactDrawers;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedCompactDrawer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FramedCompactDrawerRenderer extends DrawersRenderer {

    private final PanelBoxRenderer panelRenderer = new PanelBoxRenderer();
    private final ModularBoxRenderer invBoxRenderer = new ModularBoxRenderer();
    /** XYWH pixel coordinates for the 3-slot compact-drawer face layout (top, bottom-left, bottom-right). */
    private static final float[][] XYWH3 = { { 0, 8, 16, 8 }, { 0, 0, 8, 8 }, { 8, 0, 8, 8 } };

    private double trimWidth;
    private double trimDepth;
    private static final double lessThanHalf = 0.4375;
    private static final double moreThanHalf = 0.5625;

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockFramedCompactDrawer)) return;
        BlockFramedCompactDrawer framed = (BlockFramedCompactDrawer) block;

        IIcon icon = framed.getDefaultFaceIcon();

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
    protected void renderBaseBlock(IBlockAccess world, TileEntityDrawers tile, int x, int y, int z, BlockDrawers block,
        RenderBlocks renderer) {
        BlockFramedCompactDrawer framed = (BlockFramedCompactDrawer) block;

        ItemStack matSide = tile.getEffectiveMaterialSide();
        ItemStack matFront = tile.getEffectiveMaterialFront();
        ItemStack matTrim = tile.getEffectiveMaterialTrim();

        IIcon sideIcon = resolveIcon(matSide, framed.getDefaultFaceIcon());
        IIcon trimIcon = resolveIcon(matTrim, framed.getDefaultTrimIcon());
        IIcon frontIcon = resolveIcon(matFront, framed.getDefaultFaceIcon());

        int dir = tile.getDirection();
        int rotation = tile.getRotation();
        trimWidth = framed.getTrimWidth();
        trimDepth = framed.getTrimDepth();

        panelRenderer.setTrimWidth(trimWidth);
        panelRenderer.setTrimDepth(0);
        panelRenderer.setTrimColor(ModularBoxRenderer.COLOR_WHITE);
        panelRenderer.setPanelColor(ModularBoxRenderer.COLOR_WHITE);

        RenderHelper rh = RenderHelper.instances.get();
        rh.setColorAndBrightness(world, block, x, y, z);
        rh.state.setRotateTransform(RenderHelper.ZNEG, dir);
        if (dir <= 1) {
            // Storage Drawers 2.2.28+ lets a drawer face up or down, and spins the front grain with the
            // placer's pitch rather than using the fixed face table. Mirrors DrawersRenderer.renderBaseBlock;
            // without this a flat-mounted framed compacting drawer renders with unrotated textures.
            if (dir == 1) rh.state.setUVRotation(RenderHelper.YPOS, (4 - rotation) % 4);
            else rh.state.setUVRotation(RenderHelper.YNEG, (rotation + 2) % 4);
        } else {
            rh.state.setUVRotation(RenderHelper.YPOS, RenderHelperState.ROTATION_BY_FACE_FACE[RenderHelper.ZNEG][dir]);
        }

        int pass = ForgeHooksClient.getWorldRenderPass();
        if (pass == 0) {
            panelRenderer.setTrimIcon(trimIcon);
            panelRenderer.setPanelIcon(sideIcon);
            for (int i = 0; i < 6; i++) {
                if (i != RenderHelper.ZNEG) panelRenderer.renderFacePanel(i, world, framed, x, y, z, 0, 0, 0, 1, 1, 1);
                panelRenderer.renderFaceTrim(i, world, framed, x, y, z, 0, 0, 0, 1, 1, 1);
            }
            panelRenderer.setTrimDepth(trimDepth);
            panelRenderer.renderInteriorTrim(RenderHelper.ZNEG, world, framed, x, y, z, 0, 0, 0, 1, 1, 1);

            rh.state.flipTexture = true;

            // Top slot (slot 0)
            rh.setRenderBounds(trimWidth, trimWidth, trimDepth, lessThanHalf, lessThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, frontIcon);

            // Bottom-left slot (slot 1)
            rh.setRenderBounds(trimWidth, moreThanHalf, trimDepth, 1 - trimWidth, 1 - trimWidth, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, frontIcon);

            // Bottom-right slot (slot 2)
            rh.setRenderBounds(moreThanHalf, trimWidth, trimDepth, 1 - trimWidth, lessThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, frontIcon);

            // Slot dividers
            // Horizontal divider between top slot and bottom half (left side)
            rh.setRenderBounds(trimWidth, lessThanHalf, trimDepth, lessThanHalf, moreThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimIcon);
            // Horizontal divider between top slot and bottom half (right side)
            rh.setRenderBounds(moreThanHalf, lessThanHalf, trimDepth, 1 - trimWidth, moreThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimIcon);
            // Vertical divider between top-left and top-right
            rh.setRenderBounds(lessThanHalf, trimWidth, trimDepth, moreThanHalf, lessThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimIcon);
            // Center cross
            rh.setRenderBounds(lessThanHalf, lessThanHalf, trimDepth, moreThanHalf, moreThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimIcon);

            // Storage upgrade overlay: re-render outer trim + cut/inside faces with overlay textures
            int maxStorageLevel = tile.getMaxStorageLevel();
            if (maxStorageLevel > 1 && StorageDrawers.config.cache.renderStorageUpgrades
                && !tile.shouldHideUpgrades()) {
                IIcon overlayTrim = framed.getOverlayIconTrim(maxStorageLevel);
                if (overlayTrim != null) {
                    panelRenderer.setTrimIcon(overlayTrim);
                    panelRenderer.setPanelIcon(overlayTrim);
                    panelRenderer.setTrimColor(ModularBoxRenderer.COLOR_WHITE);
                    panelRenderer.setPanelColor(ModularBoxRenderer.COLOR_WHITE);
                    panelRenderer.setTrimDepth(0);
                    for (int i = 0; i < 6; i++) {
                        panelRenderer.renderFaceTrim(i, world, framed, x, y, z, 0, 0, 0, 1, 1, 1);
                    }
                    panelRenderer.setTrimDepth(trimDepth);
                    panelRenderer.renderInteriorTrim(RenderHelper.ZNEG, world, framed, x, y, z, 0, 0, 0, 1, 1, 1);
                }
            }

            rh.state.flipTexture = false;
        } else if (pass == 1) {
            GL11.glDepthMask(false);
            IIcon trimShadow = framed.getTrimShadowOverlay(false);
            IIcon handle = framed.getHandleOverlay();
            IIcon faceShadow = framed.getFaceShadowOverlay();
            IIcon disabledSlots = framed.getDisabledSlotsOverlay();
            panelRenderer.setTrimIcon(trimShadow);
            panelRenderer.renderFaceTrim(RenderHelper.ZNEG, world, framed, x, y, z, 0, 0, 0, 1, 1, 1);

            // Per-slot overlays: handle + face shadow + disabled slot overlay
            // Top slot (slot 0)
            rh.setRenderBounds(trimWidth, trimWidth, trimDepth, lessThanHalf, lessThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, handle);
            if (faceShadow != null) rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, faceShadow);
            if (disabledSlots != null) rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, disabledSlots);

            // Bottom-left slot (slot 1)
            rh.setRenderBounds(trimWidth, moreThanHalf, trimDepth, 1 - trimWidth, 1 - trimWidth, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, handle);
            if (faceShadow != null) rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, faceShadow);
            if (disabledSlots != null) rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, disabledSlots);

            // Bottom-right slot (slot 2)
            rh.setRenderBounds(moreThanHalf, trimWidth, trimDepth, 1 - trimWidth, lessThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, handle);
            if (faceShadow != null) rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, faceShadow);
            if (disabledSlots != null) rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, disabledSlots);

            // Slot divider shadows
            // Horizontal divider (left)
            rh.setRenderBounds(trimWidth, lessThanHalf, trimDepth, lessThanHalf, moreThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimShadow);
            // Horizontal divider (right)
            rh.setRenderBounds(moreThanHalf, lessThanHalf, trimDepth, 1 - trimWidth, moreThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimShadow);
            // Vertical divider
            rh.setRenderBounds(lessThanHalf, trimWidth, trimDepth, moreThanHalf, lessThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimShadow);
            // Center cross
            rh.setRenderBounds(lessThanHalf, lessThanHalf, trimDepth, moreThanHalf, moreThanHalf, 1);
            rh.renderFace(RenderHelper.ZNEG, world, framed, x, y, z, trimShadow);
            GL11.glDepthMask(true);
        }

        rh.state.clearRotateTransform();
        rh.state.clearUVRotation(RenderHelper.YPOS);
        rh.state.clearUVRotation(RenderHelper.YNEG);
    }

    /**
     * Renders the status indicator overlay for the 3-slot compact-drawer layout.
     *
     * <p>
     * The parent {@link DrawersRenderer#renderIndicator} only handles drawerCount 1, 2, and 4. For 3-slot
     * blocks, the count variable stays 0 so the indicator is silently skipped. There are also no
     * {@code indicator_3_on/off} textures in Storage Drawers 1.7.10 assets. This override uses the default
     * face icon with color multiplication to draw the indicator bar at the correct per-slot XYWH positions
     * for a compacting drawer.
     */
    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (!(block instanceof BlockFramedCompactDrawer)) return false;

        // Let the parent render the base block, lock, void, tape, and shroud overlays.
        // The parent's renderIndicator is a no-op for drawerCount == 3.
        boolean result = super.renderWorldBlock(world, x, y, z, block, modelId, renderer);
        if (!result) return false;

        BlockFramedCompactDrawer framed = (BlockFramedCompactDrawer) block;
        TileEntityDrawers tile = framed.getTileEntity(world, x, y, z);
        if (tile == null) return true;

        int side = tile.getDirection();
        if (side < 2 || side > 5) return true;

        if (StorageDrawers.config.cache.enableIndicatorUpgrades) {
            int level = tile.getEffectiveStatusLevel();
            if (level > 0) {
                renderIndicator3(framed, tile, world, x, y, z, side, renderer, level);
            }
        }

        return true;
    }

    private void renderIndicator3(BlockFramedCompactDrawer block, TileEntityDrawers tile, IBlockAccess world, int x,
        int y, int z, int side, RenderBlocks renderer, int level) {
        if (net.minecraftforge.client.ForgeHooksClient.getWorldRenderPass() == 0) return;

        double unit = 0.0625;

        // Indicator art occupies pixels 2-5 horizontally and 1-7 vertically in the 8x8 padded texture
        int px = 6, py = 1, pw = 4, ph = 7;
        double minX = px * unit;
        double minY = py * unit;
        double maxX = (px + pw) * unit;
        double maxY = (py + ph) * unit;
        double trimZ = block.getTrimDepth();

        RenderHelper rh = RenderHelper.instances.get();

        // Compute indicator step 0–6 from the primary drawer's fill level
        IDrawer drawer = tile.getDrawer(0);
        int step = 0;
        if (drawer != null) {
            if (level == 1) {
                step = (drawer.getMaxCapacity() > 0 && drawer.getRemainingCapacity() == 0) ? 6 : 0;
            } else {
                int cap = drawer.getMaxCapacity();
                int stored = drawer.getStoredItemCount();
                if (cap > 0 && stored > 0) {
                    double fill = Math.min((double) stored / cap, 1.0);
                    step = (int) (fill * 6);
                    if (step > 6) step = 6;
                }
            }
        }

        IIcon icon = block.getIndicator3Icon(step);

        // Sample only the indicator art region from the 8x8 padded texture.
        // Parameters are icon-space fractions (0-1), converted internally via getInterpolatedU(getInterpolatedV.
        double uMin = 0.25; // pixel 2/8
        double uMax = 0.75; // pixel 6/8
        double vMin = 0.125; // pixel 1/8
        double vMax = 1.0; // pixel 8/8

        // Render on the ZNEG face (drawer front) at the same depth as the handle.
        // Use renderPartialFace with explicit UVs to sample only the indicator art,
        // avoiding the transparent padding.
        rh.setRenderBounds(minX, minY, trimZ - 0.001, maxX, maxY, trimZ + 0.001);
        rh.state.setRotateTransform(RenderHelper.ZNEG, side);
        rh.renderPartialFace(RenderHelper.ZNEG, world, block, x, y, z, icon, uMin, vMin, uMax, vMax);
        rh.state.clearRotateTransform();
    }

    private IIcon resolveIcon(ItemStack stack, IIcon fallback) {
        if (stack == null || stack.getItem() == null) return fallback;
        Block b = Block.getBlockFromItem(stack.getItem());
        if (b == null) return fallback;
        IIcon icon = b.getIcon(4, stack.getItemDamage());
        return icon != null ? icon : fallback;
    }

    @Override
    public int getRenderId() {
        return FramedCompactDrawers.proxy.framedCompactDrawerRenderID;
    }
}
