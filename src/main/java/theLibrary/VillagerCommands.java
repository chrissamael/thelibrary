package theLibrary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VillagerCommands implements CommandExecutor {
	Connection con = null;
	VillagerListeners listeners = null;
	TheLibrary lib = null;
	
	public void setVillagerListeners(VillagerListeners listeners)
	{
		this.listeners = listeners;
	}
	public void setTheLibrary(TheLibrary lib) {this.lib=lib;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("You are not a player!");
			return true;
		}
		
		Player p = (Player) sender;
		
		if(command.getName().equalsIgnoreCase("villagersaver")) {
			p.sendMessage("you used a command :P");
			return true;
		}
		if(command.getName().equalsIgnoreCase("listvillagers")) {
			int factor = 0;
			try
			{
				factor = Integer.parseInt(args[0]);
			}catch(Exception e){}
			int lowLimit = 0+factor*10;
			int highLimit = 10+factor*10;
			
			System.out.println("Listvillagers called with factor: "+factor);
			if(con!=null)
			{
				try
				{
				p.sendMessage("Found following villagers in The Library:");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM villager ORDER BY id ASC LIMIT "+lowLimit+","+highLimit);
				while(rs.next())
				{
					p.sendMessage(""+rs.getString("id")+": '"+rs.getString("name")+"'("+rs.getFloat("posx")+" "+rs.getFloat("posy")+" "+rs.getFloat("posz")+" ["+rs.getString("world")+"]) ["+rs.getString("timestamp")+"]");
				}
				}catch(Exception e)
				{
					p.sendMessage("DB Con failed");
				}
			}
			return true;
		}
		if(command.getName().equalsIgnoreCase("findvillagers"))
		{
			double radius = 0;
			String show = "";
			try {
				radius = Integer.parseInt(args[0]);
				show = args[1];
			}catch(Exception e) {
				e.printStackTrace();
			}
			

			
			double lowX = p.getLocation().getX()-radius;
			double highX = p.getLocation().getX()+radius;
			double lowY = p.getLocation().getY()-radius;
			double highY = p.getLocation().getY()+radius;			
			double lowZ = p.getLocation().getZ()-radius;
			double highZ = p.getLocation().getZ()+radius;
			if(show.equalsIgnoreCase("show") && !listeners.getArmorStands().isEmpty()) {
				Map<UUID,String[]> armorStands = new HashMap<UUID,String[]>();
				for(UUID id : listeners.getArmorStands().keySet())
				{
					armorStands.put(id, listeners.getArmorStands().get(id));
				}
		    	for(UUID id : armorStands.keySet())
		    	{
		    		System.out.println(id.toString());
		    		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill "+id.toString());
		    		listeners.removeArmorStand(id);
		    	}
			}
			try
			{
				System.out.println();
				p.sendMessage("Found following villager(s) saved in The Library in the radius of "+radius+":");
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM villager WHERE posx<="+highX+" AND posx>="+lowX+" AND posy<="+highY+" AND posy>="+lowY+" AND posz<="+highZ+" AND posz>="+lowZ+" AND world='"+p.getLocation().getWorld().getName()+"'");
				while(rs.next())
				{
					p.sendMessage(""+rs.getString("id")+": '"+rs.getString("name")+"'("+rs.getFloat("posx")+" "+rs.getFloat("posy")+" "+rs.getFloat("posz")+" ["+rs.getString("world")+"]) ["+rs.getString("timestamp")+"]");					
					if(show.equalsIgnoreCase("show"))
					{
						UUID id = UUID.randomUUID();
						long UUIDMost = id.getMostSignificantBits();
						long UUIDLeast = id.getLeastSignificantBits();
						Bukkit.getServer().dispatchCommand(sender, "summon minecraft:armor_stand "+rs.getString("posx")+" "+(Double.parseDouble(rs.getString("posy"))+1)+" "+rs.getString("posz")+" {NoGravity:1b,Time:30,Glowing:1b,DropItem:0b,UUIDMost:"+UUIDMost+"L,UUIDLeast:"+UUIDLeast+"L}");
						autoKillArmorStand akas = new autoKillArmorStand();
						akas.setUUID(id);
						akas.setLib(lib);
						akas.start();
						listeners.addArmorStand(id,rs.getString("uuid"),p.getUniqueId().toString());
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		if(command.getName().equalsIgnoreCase("findnamedvillagers"))
		{
			String name = args[0];
			if(con != null && name != null)
			{
				try {
					p.sendMessage("Found following villager(s) saved The Library with the name '"+name+"':");
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM villager WHERE name='"+name+"'");
					while(rs.next())
					{
						p.sendMessage(""+rs.getString("id")+": '"+rs.getString("name")+"'("+rs.getFloat("posx")+" "+rs.getFloat("posy")+" "+rs.getFloat("posz")+" ["+rs.getString("world")+"]) ["+rs.getString("timestamp")+"]");					
					}

				}catch(Exception e) {e.printStackTrace();}
			}
			return true;
		}
		if(command.getName().equalsIgnoreCase("respawnvillager"))
		{	
			String mode = "";
			try
			{
			mode = args[0];
			}catch(Exception e)
			{
				p.sendMessage("§4Sorry, you have to specify the mode, e.g. /respawnvillager id 5 or /respawnvillager radius 10");
				return true;
			}
			String id = "-1";
			p.sendMessage("you send following mode: "+mode);
			try
			{
			p.sendMessage("Found following villager(s) in database:");
			Statement stmt = con.createStatement();
			String query = "";
			if(mode.equalsIgnoreCase("id"))
			{
				id = args[1];
				query = "SELECT * FROM villager WHERE id='"+id+"'";
				p.sendMessage("Set mode to id spawning");
			}
			else if(mode.equalsIgnoreCase("radius"))
			{
				Double rad = Double.parseDouble(args[1]);
				double lowX = p.getLocation().getX()-rad;
				double highX = p.getLocation().getX()+rad;
				double lowY = p.getLocation().getY()-rad;
				double highY = p.getLocation().getY()+rad;			
				double lowZ = p.getLocation().getZ()-rad;
				double highZ = p.getLocation().getZ()+rad;
				query = "SELECT * FROM villager WHERE posx<="+highX+" AND posx>="+lowX+" AND posy<="+highY+" AND posy>="+lowY+" AND posz<="+highZ+" AND posz>="+lowZ+" AND world='"+p.getLocation().getWorld().getName()+"'";
				p.sendMessage("Set mode to radial spawning");
			}
			else
			{
				p.sendMessage("§4Sorry, you have to specify the mode, e.g. /respawnvillager id 5 or /respawnvillager radius 10");
			}
			System.out.println("EXECUTING:");
			System.out.println(query);
			System.out.println();
			ResultSet rs = stmt.executeQuery(query);
			int cnt = 0;
			while(rs.next())
			{
				if(p.getLocation().getWorld().getName().equals(rs.getString("world")))
				{
					Bukkit.getServer().dispatchCommand(sender, "summon villager "+rs.getString("posx")+" "+rs.getString("posy")+" "+rs.getString("posz")+" "+rs.getString("nbt"));
					cnt++;
				}
				else
				{
					p.sendMessage("§4Sorry, you have to be in the correct dimension ("+rs.getString("world")+") to respawn this villager.");
				}
			}
			p.sendMessage("Respawned "+cnt+" villagers");
			}catch(Exception e)
			{
				p.sendMessage("DB Con failed");
			}
			
			return true;
		}
		if(command.getName().equalsIgnoreCase("deletevillager"))
		{
			String id = args[0];
			try
			{
				Statement stmt = con.createStatement();
				stmt.executeUpdate("DELETE FROM villager WHERE id = '"+id+"'");
				p.sendMessage("Villager deleted from list.");
			}catch(Exception e) {p.sendMessage("Villager could not be deleted.");}
			return true;
		}
		return false;
	}
	
	public void setConnection(Connection con)
	{
		this.con=con;
	}

}
