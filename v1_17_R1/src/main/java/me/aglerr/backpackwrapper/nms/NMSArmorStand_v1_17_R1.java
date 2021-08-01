package me.aglerr.backpackwrapper.nms;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class NMSArmorStand_v1_17_R1 implements NMSArmorStand{

    private final Map<Player, NMSBackpack> armorStandMap = new HashMap<>();

    @Override
    public void setHeadRotation(Entity entity, float yaw) {
        ((CraftArmorStand) entity).getHandle().setHeadRotation(yaw);
    }

    @Override
    public void setHeadRotation(Player player, float yaw) {
        if(!armorStandMap.containsKey(player)) return;

        NMSBackpack backpack = armorStandMap.get(player);
        sendPacket(new PacketPlayOutEntityHeadRotation(backpack.getArmorStand(), ((byte) yaw)), null);
    }

    @Override
    public void createArmorStand(Player player, Player target, ItemStack stack) {
        System.out.println("Equipping backpack for " + player.getName());
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        EntityArmorStand stand = new EntityArmorStand(EntityTypes.c, worldServer);

        stand.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
        stand.setBasePlate(false);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.collides = false;

        NMSBackpack nmsBackpack = armorStandMap.get(player);

        if(nmsBackpack == null){
            nmsBackpack = new NMSBackpack(player, stand, stack);
            armorStandMap.put(player, nmsBackpack);
        }

        if(nmsBackpack.isShown(target)){
            System.out.println("Cancelled, already shown!");
            return;
        }

        sendPacket(new PacketPlayOutSpawnEntityLiving(nmsBackpack.getArmorStand()), target);

        final List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> equipmentList = Collections.singletonList(
            new Pair<>(EnumItemSlot.f, CraftItemStack.asNMSCopy(stack))
        );

        sendPacket(new PacketPlayOutEntityMetadata(nmsBackpack.getArmorStand().getId(), stand.getDataWatcher(), false), target);
        sendPacket(new PacketPlayOutEntityEquipment(nmsBackpack.getArmorStand().getId(), equipmentList), target);

        nmsBackpack.getArmorStand().startRiding(((CraftEntity) player).getHandle());

        if(target == null){
            nmsBackpack.addAllPlayers();
        } else {
            nmsBackpack.addPlayer(target);
        }
    }

    @Override
    public void destroyArmorStand(Player player) {
        if(!armorStandMap.containsKey(player)) return;
        System.out.println("Destroying backpack for " + player.getName());

        // Remove the nms backpack from the map and keep the reference
        NMSBackpack nmsBackpack = armorStandMap.remove(player);
        // Make the armor stand stop riding first
        nmsBackpack.getArmorStand().stopRiding();
        // And then destroy the armor stand
        sendPacket(new PacketPlayOutEntityDestroy(nmsBackpack.getArmorStand().getId()), null);
    }

    @Override
    public void rotateArmorStand() {
        armorStandMap.forEach((player, nmsBackpack) -> {
            if(nmsBackpack.getArmorStand().getVehicle() == null){
                nmsBackpack.getArmorStand().startRiding(((CraftEntity) player).getHandle());
            }
            setHeadRotation(player, player.getLocation().getYaw());
        });
    }

    @Override
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        // Get the player who joined the server
        Player joined = event.getPlayer();
        // Loop through all backpack hashmap and send the packet
        armorStandMap.forEach((player, backpack) ->
                createArmorStand(player, joined, backpack.getItemStack()));
    }

    @Override
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        // Get the player who quited the server
        Player quited = event.getPlayer();
        // If the quited player have backpack, remove the backpack
        destroyArmorStand(quited);
        // Loop through all armor stand hash map
        armorStandMap.forEach((player, nmsBackpack) ->
                nmsBackpack.removePlayer(quited));
    }

    /**
     * @param player
     * @return
     */
    @Override
    public boolean isWearingBackpack(Player player) {
        return armorStandMap.containsKey(player);
    }

    private void sendPacket(Packet packet, Player player){
        if(player == null){
            Bukkit.getOnlinePlayers().forEach(online -> ((CraftPlayer) online).getHandle().b.sendPacket(packet));
            return;
        }
        ((CraftPlayer) player).getHandle().b.sendPacket(packet);
    }

}
