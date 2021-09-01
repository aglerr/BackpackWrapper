package me.aglerr.backpackwrapper;

import me.aglerr.backpackwrapper.nms.NMSArmorStand;
import org.bukkit.Bukkit;

public class BackpackWrapper {

    private static NMSArmorStand nmsArmorStand;

    private static boolean IS_LOADED = false;

    public static NMSArmorStand getNMSArmorStand(){
        if(nmsArmorStand == null && !IS_LOADED){
            loadNMS();
        }
        return nmsArmorStand;
    }

    public static void loadNMS(){
        if(IS_LOADED){
            return;
        }

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try{
            nmsArmorStand = (NMSArmorStand) Class.forName("me.ziwoxyt.backpack.utils.nms.NMSArmorStand_" + version).newInstance();
            IS_LOADED = true;
        } catch (Exception ex){
            System.out.println("BackpackCosmetics doesn't support " + version + " - shutting down...");
            Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("BackpackCosmetics"));
        }
    }

}
