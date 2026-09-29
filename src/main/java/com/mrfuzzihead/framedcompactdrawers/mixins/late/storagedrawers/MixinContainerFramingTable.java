package com.mrfuzzihead.framedcompactdrawers.mixins.late.storagedrawers;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jaquadro.minecraft.storagedrawers.inventory.ContainerFramingTable;
import com.jaquadro.minecraft.storagedrawers.item.ItemCustomDrawers;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedCompactDrawer;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedController;
import com.mrfuzzihead.framedcompactdrawers.block.BlockFramedSlave;

@Mixin(ContainerFramingTable.class)
public class MixinContainerFramingTable {

    @Shadow(remap = false)
    private IInventory tableInventory;

    @Shadow(remap = false)
    private IInventory craftResult;

    @Shadow(remap = false)
    private Slot inputSlot;

    @Shadow(remap = false)
    private Slot materialSideSlot;

    @Shadow(remap = false)
    private Slot materialTrimSlot;

    @Shadow(remap = false)
    private Slot materialFrontSlot;

    /**
     * Storage Drawers builds its own framing-table result in {@code onCraftMatrixChanged} and finishes by
     * unconditionally clearing the result slot, because it only recognises {@code BlockDrawersCustom} and
     * {@code BlockTrimCustom}. This block is not either of those, so Storage Drawers always clears the result
     * for it.
     *
     * <p>
     * This injects at {@code TAIL} rather than {@code HEAD} so Storage Drawers' method always runs to
     * completion and we only overwrite the slot afterwards. Cancelling at {@code HEAD} instead would skip
     * whatever bookkeeping Storage Drawers does around the result, which is a moving target across versions.
     *
     * <p>
     * Storage Drawers also treats the side material as mandatory
     * ({@code block instanceof BlockDrawersCustom && matSide != null}) and offers no feedback when it is
     * missing, so a player who fills only the trim or front slot gets a silently empty result. The framed
     * blocks are happy to render with a single material, so promote whichever slot was filled.
     */
    @Inject(method = "onCraftMatrixChanged(Lnet/minecraft/inventory/IInventory;)V", at = @At("TAIL"), remap = false)
    private void framedCompactDrawers$handleFramedBlocks(IInventory inventory, CallbackInfo ci) {
        ItemStack target = tableInventory.getStackInSlot(inputSlot.getSlotIndex());
        if (target == null) return;

        Block block = Block.getBlockFromItem(target.getItem());
        if (!isFramedBlock(block)) return;

        ItemStack matSide = tableInventory.getStackInSlot(materialSideSlot.getSlotIndex());
        ItemStack matTrim = tableInventory.getStackInSlot(materialTrimSlot.getSlotIndex());
        ItemStack matFront = tableInventory.getStackInSlot(materialFrontSlot.getSlotIndex());

        if (matSide == null) {
            if (matTrim != null) {
                matSide = matTrim;
                matTrim = null;
            } else if (matFront != null) {
                matSide = matFront;
                matFront = null;
            } else {
                return;
            }
        }

        // Returns null if the block's ItemBlock is not an ItemCustomDrawers, which cannot happen for the
        // blocks this mixin handles.
        ItemStack result = ItemCustomDrawers.makeItemStack(block, 1, matSide, matTrim, matFront);
        if (result == null) return;

        craftResult.setInventorySlotContents(0, result);
    }

    private static boolean isFramedBlock(Block block) {
        return block instanceof BlockFramedCompactDrawer || block instanceof BlockFramedController
            || block instanceof BlockFramedSlave;
    }
}
