package theLibrary;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public final class TheLibrary extends JavaPlugin {
	VillagerListeners villagerlisteners;
	VillagerCommands villagercommands;
	
	public Connection getCon() {
		return con;
	}

	private Connection con = null;
	
	
    @Override
    public void onEnable() {
        // TODO Insert logic to be performed when the plugin is enabled
    	villagerlisteners = new VillagerListeners();
    	villagercommands = new VillagerCommands();
    	villagercommands.setVillagerListeners(villagerlisteners);
    	getCommand("listvillagers").setExecutor(villagercommands);
    	getCommand("respawnvillager").setExecutor(villagercommands);
    	getCommand("deletevillager").setExecutor(villagercommands);
    	getCommand("findnamedvillagers").setExecutor(villagercommands);
    	getCommand("findvillagers").setExecutor(villagercommands);
    	Bukkit.getPluginManager().registerEvents(villagerlisteners, this);
    	
    	if((con = getDatabase()) == null)
    	{
    		con=setupDatabase();
    	}
    	villagerlisteners.setConnection(con);
    	villagerlisteners.setLib(this);
    	villagercommands.setConnection(con);
    	villagercommands.setTheLibrary(this);
    }
    
    
    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
    	for(UUID id : villagerlisteners.getArmorStands().keySet())
    	{
    		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill "+id);
    	}
        try
        {
          if(con != null)
            con.close();
        }
        catch(SQLException e)
        {
          // connection close failed.
          getLogger().info("Database connection could not be closed!");
        }
    	getLogger().info("Database unloaded!");
    }  
    
    
    private Connection setupDatabase()
    {
    	try {
    		new File("plugins"+File.separator+"thelibrary").mkdirs();
    	con = DriverManager.getConnection("jdbc:sqlite:plugins"+File.separator+"thelibrary"+File.separator+"villagers.db");
        Statement statement = con.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.
        statement.executeUpdate("drop table if exists villager");
        statement.executeUpdate("create table villager ("
        		+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ "uuid string UNIQUE,"
        		+ " name string,"
        		+ " posx float,"
        		+ " posy float,"
        		+ " posz float,"
        		+ " world string,"
        		+ " nbt string,"
        		+ " timestamp string)");
        getLogger().info("Database has been created");
        return con;
    	}catch(Exception e)
    	{
            getLogger().info("Database creation failed!");
            return null;
    	}
    }
    
    private Connection getDatabase()
    {
    	try
    	{
    		
    		Connection connection = DriverManager.getConnection("jdbc:sqlite:plugins"+File.separator+"thelibrary"+File.separator+"villagers.db");
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery("SELECT * FROM villager");
    		int cnt = 0;
    		while(rs.next())
    		{
    			cnt++;
    		}
    		getLogger().info("Database connection established, currently saved "+cnt+" villagers in The Library");
    	return connection;
    	}
    	catch(Exception e)
    	{
    		getLogger().info("Database connection could not be established!");
    		return setupDatabase();
    	}
    }
    
}
