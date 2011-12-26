import java.util.logging.Logger;

public class onLotto extends Plugin {
	Logger log = Logger.getLogger("Minecraft");
	OLListener OLL;
	OLData OLD;
	String name = "onLotto";
	String version = "1.0";
	String author = "darkdiplomat";
	
	public void disable(){
		OLL.OLT.cancelTimer();
		etc.getInstance().removeCommand("/onlotto");
		log.info(name + " version " + version + " disabled!");
	}
	
	public void enable() {
		etc.getInstance().addCommand("/lotto", " - display info for ItemLottery");
		log.info(name + " version " + version + " by " + author + " enabled!");
	}
	
	public void initialize() {
		OLD = new OLData();
		OLL = new OLListener(OLD);
		etc.getLoader().addListener(PluginLoader.Hook.LOGIN, OLL, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, OLL, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.SERVERCOMMAND, OLL, this, PluginListener.Priority.MEDIUM);
		OLL.OLT.startTimer();
		log.info(name + " version " + version + " initialized");
	}
}
