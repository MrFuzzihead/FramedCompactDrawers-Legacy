package com.mrfuzzihead.framedcompactdrawers.block.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;

public class TileFramedController extends TileEntityController {

    private ItemStack matSide;
    private ItemStack matTrim;
    private ItemStack matFront;

    public TileFramedController() {
        super();
    }

    public ItemStack getMaterialSide() {
        return matSide;
    }

    public void setMaterialSide(ItemStack stack) {
        this.matSide = stack;
    }

    public ItemStack getMaterialTrim() {
        return matTrim;
    }

    public void setMaterialTrim(ItemStack stack) {
        this.matTrim = stack;
    }

    public ItemStack getMaterialFront() {
        return matFront;
    }

    public void setMaterialFront(ItemStack stack) {
        this.matFront = stack;
    }

    /**
     * Mirrors the fallback chain on {@code TileEntityDrawers.getEffectiveMaterial*}. A block produced by this
     * mod's recipe carries no {@code MatS}/{@code MatT}/{@code MatF} NBT, so the raw getters legitimately
     * return null until the block has been through the Storage Drawers framing table. The renderers resolve
     * these and then fall back to the raw default icons, which is why an unframed block still looks and
     * behaves like a block instead of failing to render.
     */
    public ItemStack getEffectiveMaterialSide() {
        if (matSide != null) return matSide;
        if (matTrim != null) return matTrim;
        return matFront;
    }

    public ItemStack getEffectiveMaterialTrim() {
        if (matTrim != null) return matTrim;
        return getEffectiveMaterialSide();
    }

    public ItemStack getEffectiveMaterialFront() {
        if (matFront != null) return matFront;
        return getEffectiveMaterialSide();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if (tag.hasKey("MatS")) {
            matSide = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("MatS"));
        }
        if (tag.hasKey("MatT")) {
            matTrim = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("MatT"));
        }
        if (tag.hasKey("MatF")) {
            matFront = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("MatF"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        if (matSide != null) {
            NBTTagCompound sideTag = new NBTTagCompound();
            matSide.writeToNBT(sideTag);
            tag.setTag("MatS", sideTag);
        }
        if (matTrim != null) {
            NBTTagCompound trimTag = new NBTTagCompound();
            matTrim.writeToNBT(trimTag);
            tag.setTag("MatT", trimTag);
        }
        if (matFront != null) {
            NBTTagCompound frontTag = new NBTTagCompound();
            matFront.writeToNBT(frontTag);
            tag.setTag("MatF", frontTag);
        }

        super.writeToNBT(tag);
    }
}
