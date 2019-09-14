package theLibrary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_14_R1.*;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;


public class VillagerListeners implements Listener {

	Map<UUID,String[]> armorStands = new HashMap<UUID,String[]>();
	TheLibrary lib = null;
	public void setLib(TheLibrary lib) {
		this.lib = lib;
	}
	
	/*
	 * (id integer, uuid string, name string, posx integer, posy integer, posz integer, nbt string)
	 */
	Connection con = null;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	
	@EventHandler
	public void onVillagerDeath(EntityDeathEvent e)
	{
		if(con != null && e.getEntity() instanceof Villager)
		{
			try {
				Statement stmt = con.createStatement();
				stmt.executeQuery("DELETE FROM villager WHERE uuid='"+e.getEntity().getUniqueId()+"'");
				}catch(Exception exc)
			{
				
			}
		}
	}
	/*
	 * This EventHandler is the first try to implement a "delete on transform to witch" logic
	 * Sadly, the witch has no data which can 
	@EventHandler
	public void onVillagerLightningStrike(EntityTransformEvent e)
	{
		ArrayList<String> ids = new ArrayList<String>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT uuid FROM villager");
			while(rs.next())
			{
				ids.add(rs.getString("uuid"));
				System.out.println(rs.getString("uuid"));
			}
			System.out.println("looking for: "+e.getTransformedEntity().getUniqueId().toString());
			if(e.getTransformReason() == EntityTransformEvent.TransformReason.LIGHTNING && ids.contains(e.getTransformedEntity().getUniqueId().toString()))
			{
				System.out.println("found villager struck by lightning");
				stmt.executeQuery("DELETE FROM villager WHERE uuid='"+e.getEntity().getUniqueId()+"'");
			}
		}catch(Exception exc)
		{
			exc.printStackTrace();
		}

	}
	*/
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e)
	{
		if(con != null && e.getChunk().getEntities().length != 0)
		{
			for(org.bukkit.entity.Entity ent : e.getChunk().getEntities())
			{
				if(ent instanceof Villager)
				{
					updateVillagerData((Villager)ent);
				}
			}
		}
	}
	
	private void createVillagerData(Villager villager)
	{
		net.minecraft.server.v1_14_R1.Entity nmsEntity = ((CraftEntity)villager).getHandle();
		NBTTagCompound tag = new NBTTagCompound();
		nmsEntity.save(tag);
		try
		{
		PreparedStatement stmt = con.prepareStatement("INSERT INTO villager (uuid,name,posx,posy,posz,world,nbt,timestamp) VALUES ('"+villager.getUniqueId()+"','"+villager.getCustomName()+"',"
				+ villager.getLocation().getX()+","+villager.getLocation().getY()+","+villager.getLocation().getZ()+",'"+villager.getLocation().getWorld().getName()+"','"+tag.asString().replaceAll("'", "''")+"','"+sdf.format(System.currentTimeMillis())+"')");
		stmt.executeUpdate();
		}catch(Exception exc2)
		{
			exc2.printStackTrace();
		}
	}
	
	private boolean updateVillagerData(Villager villager)
	{
		net.minecraft.server.v1_14_R1.Entity nmsEntity = ((CraftEntity)villager).getHandle();
		NBTTagCompound tag = new NBTTagCompound();
		nmsEntity.save(tag);
		//nmsEntity.c(tag);
		try
		{
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM villager WHERE uuid='"+villager.getUniqueId()+"'");
		rs.next();
		//player.sendMessage("result: "+rs.getString("name"));
		if(!rs.isClosed())
		{
		String nbtDB = rs.getString("nbt").replaceAll("Spigot.ticksLived:[0123456789]*,", "");
		String nbtWorld = tag.asString().replaceAll("Spigot.ticksLived:[0123456789]*,", "");
		if(!nbtDB.equals(nbtWorld))
		{
			
			PreparedStatement updateStmt = con.prepareStatement("UPDATE villager SET "
					+ "posx='"+villager.getLocation().getX()+"',"
					+ "posy='"+villager.getLocation().getY()+"',"
					+ "posz='"+villager.getLocation().getZ()+"',"
					+ "world='"+villager.getLocation().getWorld().getName()+"',"
					+ "nbt='"+tag.asString().replaceAll("'", "''")+"',"
					+ "name='"+villager.getCustomName()+"',"
					+ "timestamp='"+sdf.format(System.currentTimeMillis())+"' "
					+ "WHERE uuid='"+villager.getUniqueId()+"'");
			updateStmt.executeUpdate();		
			return true;
		}
		else
		{

		}

		}
		else
		{
			return false;
		}

		}catch(SQLException exc)
		{
			exc.printStackTrace();
				return false;
		}
		return false;
	}
	
	@EventHandler
	public void onInteractBy(PlayerInteractAtEntityEvent e)
	{
		//e.getPlayer().sendMessage("You clicked on "+e.getRightClicked().getUniqueId());
		//System.out.println("Following UUIDs are known:");
		if(con != null && e.getRightClicked() instanceof ArmorStand)
		{
			for(UUID id : armorStands.keySet())
			{
				//System.out.println(id);
				if(id.toString().equalsIgnoreCase(e.getRightClicked().getUniqueId().toString()) &&
						armorStands.get(id)[1].equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
					Player p = e.getPlayer();
					String query = "SELECT * FROM villager WHERE uuid='"+armorStands.get(id)[0]+"'";
					
					try
					{
						Statement stmt = con.createStatement();
						ResultSet rs = stmt.executeQuery(query);
						while(rs.next())
						{
							if(e.getPlayer().getLocation().getWorld().getName().equals(rs.getString("world")))
							{
								armorStands.remove(id);
								Bukkit.getServer().dispatchCommand(p, "kill "+id);
								new BukkitRunnable() {

									@Override
									public void run() {
										try
										{
											Statement stmt = con.createStatement();
											ResultSet rs = stmt.executeQuery(query);
										Bukkit.getServer().dispatchCommand(p, "summon villager "+rs.getString("posx")+" "+rs.getString("posy")+" "+rs.getString("posz")+" "+rs.getString("nbt"));
										}catch(Exception e) {
											e.printStackTrace();
										}
									}
									
								}.runTaskLater(lib,5);
								
							}
							else
							{
								p.sendMessage("§4Sorry, you have to be in the correct dimension ("+rs.getString("world")+") to respawn this villager.");
							}
						}
						
					}catch(Exception exc)
					{
						exc.printStackTrace();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		if(con != null && e.getRightClicked() instanceof Villager)
		{
			Villager villager = (Villager) e.getRightClicked();
			//player.sendMessage("this villager is in the "+villager.getLocation().getWorld());
			if(!updateVillagerData(villager))
			{
				createVillagerData(villager);
			}
		}
	}
	
	public void setConnection(Connection con)
	{
		this.con=con;
	}
	public Map<UUID,String[]> getArmorStands(){
		return armorStands;
	}
	public void addArmorStand(UUID uuid, String villagerUUID, String playerUUID) {
		String[] ids = new String[2];
		ids[0] = villagerUUID;
		ids[1] = playerUUID;
		armorStands.put(uuid,ids);
	}
	public void removeArmorStand(UUID uuid) {
		armorStands.remove(uuid);
	}
}
