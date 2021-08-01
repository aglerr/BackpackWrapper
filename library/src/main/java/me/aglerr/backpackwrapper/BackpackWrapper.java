package me.aglerr.backpackwrapper;

import me.aglerr.backpackwrapper.nms.NMSArmorStand;
import org.bukkit.Bukkit;

public class BackpackWrapper {

    private static NMSArmorStand nmsArmorStand;

    public static NMSArmorStand getNMSArmorStand(){
        if(nmsArmorStand == null){
            loadNMS();
        }
        return nmsArmorStand;
    }

    private static void loadNMS(){
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try{
            nmsArmorStand = (NMSArmorStand) Class.forName("me.aglerr.backpackwrapper.nms.NMSArmorStand_" + version).newInstance();
        } catch (Exception ex){
            System.out.println("BackpackCosmetics doesn't support " + version + " - shutting down...");
            Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("BackpackCosmetics"));
        }
    }

}
