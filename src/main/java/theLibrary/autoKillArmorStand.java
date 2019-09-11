package theLibrary;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class autoKillArmorStand extends Thread {

	UUID id;
	TheLibrary lib;
	public void run()
	{

		try {
			TimeUnit.SECONDS.sleep(30);
			lib.villagerlisteners.removeArmorStand(id);
			new BukkitRunnable() {

				@Override
				public void run() {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill "+id);
					
				}
				
			}.runTask(lib);
			
			

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setUUID(UUID id) {this.id=id;}
	public void setLib(TheLibrary lib) {this.lib=lib;}
}
