# thelibrary

This is a Spigot Plugin for Minecraft 1.14.4. This version of Minecraft suffers from a bug which causes villagers to vanish.
To help with this problem 'The Library' was developed. Every time a Player interacts with a villager, this villagers data (position, name and NBT data) is saved or updated, whatever is necessary. This also happens on chunk unload.

This plugin has following commands:

# Commands:

  listvillagers:
  
    description: Lists all villagers saved in The Library
    
    usage: /listvillagers or /listvillagers 1 for second page, and so on    
  respawnvillager:
  
    description: Returns villagers from The Library, either by radius or via specific id
    
    usage: /respawnvillager radius 20 or /raspawnvillager id 2
  deletevillager:
  
    description: Deletes a villager from The Library
    
    usage: /deletevillager 1
  findnamedvillagers:
  
    description: Searches The Library for a Villager with the given name
   
    usage: /findnamedvillagers "Donna Noble"
  findvillagers:
  
    description: Searches The Library for a Villager in a given radius around the player. 
    Also has the option to highlight locations of missing villagers with the use of glowing armor stands. 
    These despawn after 30 seconds or get replaced by the villager on right clicking the armor stand.
    
    usage: /findvillagers 50 [show]
