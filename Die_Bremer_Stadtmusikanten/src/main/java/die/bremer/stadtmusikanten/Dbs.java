/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package die.bremer.stadtmusikanten;
import org.bukkit.plugin.java.JavaPlugin;

public class Dbs extends JavaPlugin {

    /**
     * Plugin ON
     */
    @Override
    public void onEnable(){
        getLogger().info("Plugin Enabled.");
    }

    /**
     * Plugin OFF
     */    
    @Override
    public void onDisable(){
        getLogger().info("Plugin Disabled.");
    }
}
