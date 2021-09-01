package me.aglerr.backpackwrapper.nms;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NMSArmorStand_v1_16_R3 implements NMSArmorStand{

    private final Map<Player, NMSBackpack_v1_16_R3> armorStandMap = new HashMap<>();
    private final Map<Player, NMSEquipment_v1_16_R3> equipmentMap = new HashMap<>();

    @Override
    public void setHeadRotation(Entity entity, float yaw) {
        ((CraftArmorStand) entity).getHandle().setHeadRotation(yaw);
    }

    @Override
    public void setHeadRotation(Player player, float yaw) {
        if(!armorStandMap.containsKey(player)) return;

        NMSBackpack_v1_16_R3 backpack = armorStandMap.get(player);

        byte finalPitch = (byte) player.getLocation().getPitch();
        byte finalYaw = (byte) (yaw * 256 / 360);

        PacketPlayOutEntity.PacketPlayOutEntityLook entityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(backpack.getArmorStand().getId(), finalYaw, finalPitch, true);
        sendPacket(entityLook, null);
        sendPacket(new PacketPlayOutEntityHeadRotation(backpack.getArmorStand(), finalYaw), null);
    }

    @Override
    public void createArmorStand(Player player, Player target, ItemStack stack) {
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        EntityArmorStand stand = new EntityArmorStand(EntityTypes.ARMOR_STAND, worldServer);

        stand.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
        stand.setBasePlate(true);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.collides = false;

        NMSBackpack_v1_16_R3 nmsBackpack = armorStandMap.get(player);

        if(nmsBackpack == null){
            nmsBackpack = new NMSBackpack_v1_16_R3(player, stand, stack);
            armorStandMap.put(player, nmsBackpack);
        }

        if(nmsBackpack.isShown(target)){
            return;
        }

        sendPacket(new PacketPlayOutSpawnEntityLiving(nmsBackpack.getArmorStand()), target);

        final List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipmentList = Collections.singletonList(
                new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(stack))
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

        // Remove the nms backpack from the map and keep the reference
        NMSBackpack_v1_16_R3 nmsBackpack = armorStandMap.remove(player);
        // Make the armor stand stop riding first
        nmsBackpack.getArmorStand().stopRiding();
        nmsBackpack.getArmorStand().killEntity();
        // And then destroy the armor stand
        sendPacket(new PacketPlayOutEntityDestroy(nmsBackpack.getArmorStand().getId()), null);
    }

    @Override
    public void hideAllArmorStand(Player target) {
        // Loop through all armor stand
        armorStandMap.forEach((player, backpack) -> {
            // Make the armor stand stop riding, and then destroy it
            backpack.getArmorStand().stopRiding();
            sendPacket(new PacketPlayOutEntityDestroy(backpack.getArmorStand().getId()), target);
            backpack.removePlayer(target);
        });
    }

    @Override
    public void showAllArmorStand(Player target) {
        // Loop through all armor stand
        armorStandMap.forEach((player, backpack) ->
                createArmorStand(player, target, backpack.getItemStack()));
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
        equipmentMap.forEach((player, equipment) ->
                equipFakeArmor(player, joined, equipment.getModifiedHelmet()));
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

    @Override
    public boolean isWearingBackpack(Player player) {
        return armorStandMap.containsKey(player);
    }

    @Override
    public void equipFakeArmor(Player player, Player target, ItemStack stack) {

        EntityPlayer craftPlayer = ((CraftPlayer) player).getHandle();

        // Get all armor that player using right now
        ItemStack helmet = player.getInventory().getHelmet();

        // Store the armor to the hashmap
        final List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> defaultEquipment = Collections.singletonList(
                new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(helmet))
        );

        // Finally store it to the map
        NMSEquipment_v1_16_R3 nmsEquipment;
        if (equipmentMap.containsKey(player)){
            nmsEquipment = equipmentMap.get(player);
        } else {
            nmsEquipment = new NMSEquipment_v1_16_R3(defaultEquipment, helmet, copyItemMeta(helmet, stack));
            equipmentMap.put(player, nmsEquipment);
        }

        final List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> modifiedEquipment = Collections.singletonList(
                new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(nmsEquipment.getModifiedHelmet()))
        );

        sendPacket(new PacketPlayOutEntityEquipment(craftPlayer.getId(), modifiedEquipment), target);
    }

    @Override
    public void unequipFakeArmor(Player player) {
        // If player is not wearing fake armor, return
        if(!equipmentMap.containsKey(player)) return;

        EntityPlayer craftPlayer = ((CraftPlayer) player).getHandle();

        // Set the equipment to the default, and then remove
        final NMSEquipment_v1_16_R3 nmsEquipment = equipmentMap.remove(player);
        sendPacket(new PacketPlayOutEntityEquipment(craftPlayer.getId(), nmsEquipment.getDefaultEquipment()), null);
    }

    @Override
    public boolean isWearingFakeArmor(Player player) {
        return equipmentMap.containsKey(player);
    }

    private void sendPacket(Packet packet, Player player){
        if(player == null){
            Bukkit.getOnlinePlayers().forEach(online -> ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet));
            return;
        }
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private ItemStack copyItemMeta(ItemStack from, ItemStack to){
        if(from == null){
            return to;
        }

        if(!from.hasItemMeta()){
            return to;
        }

        int customModelData = to.getItemMeta().getCustomModelData();
        to.setItemMeta(from.getItemMeta());

        ItemMeta toMeta = to.getItemMeta();
        toMeta.setCustomModelData(customModelData);

        to.setItemMeta(toMeta);
        return to;
    }

}
