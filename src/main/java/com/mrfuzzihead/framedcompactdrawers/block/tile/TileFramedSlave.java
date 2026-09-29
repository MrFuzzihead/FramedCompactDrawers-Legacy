package com.mrfuzzihead.framedcompactdrawers.block.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntitySlave;

public class TileFramedSlave extends TileEntitySlave {

    private ItemStack matSide;
    private ItemStack matTrim;
    private ItemStack matFront;

    public TileFramedSlave() {
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

    /** See {@link TileFramedController#getEffectiveMaterialSide()} for why the fallback chain exists. */
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
        super.writeToNBT(tag);

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
    }
}
