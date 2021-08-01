package me.aglerr.backpackwrapper.nms;

import net.minecraft.server.v1_16_R3.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class NMSBackpack {

    private final Player owner;
    private final EntityArmorStand armorStand;
    private final ItemStack itemStack;
    private final Set<Player> shown = new HashSet<>();

    public NMSBackpack(Player owner, EntityArmorStand armorStand, ItemStack itemStack) {
        this.owner = owner;
        this.armorStand = armorStand;
        this.itemStack = itemStack;
    }

    public Player getOwner() {
        return owner;
    }

    public EntityArmorStand getArmorStand() {
        return armorStand;
    }

    public Set<Player> getShown() {
        return shown;
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    public boolean isShown(Player player){
        return shown.contains(player);
    }

    public void addPlayer(Player player){
        if(player == null) return;

        shown.add(player);
    }

    public void addAllPlayers(){
        shown.addAll(Bukkit.getOnlinePlayers());
    }

    public void removePlayer(Player player){
        if(player == null) return;

        shown.remove(player);
    }

    public void removeAllPlayers(){
        shown.clear();
    }

}
