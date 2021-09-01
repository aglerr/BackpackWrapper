package me.aglerr.backpackwrapper.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public interface NMSArmorStand {

    /**
     * Set head rotation with craftbukkit method
     *
     * @param entity - the entity that will be rotated
     * @param yaw - the yaw of the head rotation
     */
    void setHeadRotation(Entity entity, float yaw);

    /**
     * Set head rotation with packets
     *
     * @param player - the owner of the backpack
     * @param yaw - the yaw of the head rotation
     */
    void setHeadRotation(Player player, float yaw);

    /**
     * Create an armor stand with packets and store it on the
     * hashmap, basically spawn a backpack for the player
     *
     * @param owner - The player that's gonna wear the backpack
     * @param target - The player that gonna see the backpack
     * @param stack - The itemstack of the backpack
     */
    void createArmorStand(Player owner, Player target, ItemStack stack);

    /**
     * Destroy the entity (backpack) using packets and removes them
     * from the hashmap
     *
     * @param player -
     */
    void destroyArmorStand(Player player);

    void hideAllArmorStand(Player target);

    void showAllArmorStand(Player target);

    /**
     * Task that will run each tick, the task is to make armor stand (backpack)
     * to follow the owner head rotation
     */
    void rotateArmorStand();

    /**
     *
     * @param event
     */
    void handlePlayerJoinEvent(PlayerJoinEvent event);

    /**
     *
     * @param event
     */
    void handlePlayerQuitEvent(PlayerQuitEvent event);

    /**
     *
     * @param player
     * @return
     */
    boolean isWearingBackpack(Player player);

    void equipFakeArmor(Player player, Player target, ItemStack stack);

    void unequipFakeArmor(Player player);

    boolean isWearingFakeArmor(Player player);

}
