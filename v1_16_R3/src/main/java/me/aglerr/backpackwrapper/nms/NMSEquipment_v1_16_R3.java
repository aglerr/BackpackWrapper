package me.aglerr.backpackwrapper.nms;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NMSEquipment_v1_16_R3 {

    private final List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> defaultEquipment;
    private final ItemStack defaultHelmet;
    private final ItemStack modifiedHelmet;

    public NMSEquipment_v1_16_R3(List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> defaultEquipment, ItemStack defaultHelmet, ItemStack modifiedHelmet) {
        this.defaultEquipment = defaultEquipment;
        this.defaultHelmet = defaultHelmet;
        this.modifiedHelmet = modifiedHelmet;
    }

    public List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> getDefaultEquipment() {
        return defaultEquipment;
    }

    public ItemStack getDefaultHelmet() {
        return defaultHelmet;
    }

    public ItemStack getModifiedHelmet() {
        return modifiedHelmet;
    }
}
