
public class OLListener extends PluginListener {
	OLActions OLA;
	OLData OLD;
	OLTimer OLT;
	
	public OLListener(OLData OLD){
		this.OLD = OLD;
		OLA = new OLActions(OLD, this);
		OLT = new OLTimer(OLA, OLD);
	}
	
	public boolean onCommand(Player player, String[] cmd){
		if(cmd[0].equals("/onlotto")){
			if(cmd.length < 2){
				player.sendMessage("§2------§donLotto§2------");
				player.sendMessage("§dVersion - §21.0 §dAuthor - §2darkdiplomat");
				player.sendMessage("§dJust be online durring drawing for a chance to WIN!");
				player.sendMessage("§d/onLotto time §b- displays time till drawing");
				if(player.isAdmin()){
					player.sendMessage("§d/onlotto broadcast §b- broadcast time till drawing");
					player.sendMessage("§d/onlotto draw §b- draws lotto immediately");
				}
				return true;
			}
			else{
				if(cmd[1].equals("time")){
					return OLA.displayTimeTill(player);
				}
				else if(cmd[1].equals("broadcast")){
					if(player.isAdmin()){
						return OLA.broadcastTimeTill();
					}
					else{
						player.sendMessage("§2[§donLotto§2]§c You do not have permission to use that command!");
						return true;
					}
				}
				else if(cmd[1].equals("draw")){
					if(player.isAdmin()){
						return OLA.drawNOW();
					}
					else{
						player.sendMessage("§2[§donLotto§2]§c You do not have permission to use that command!");
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean onConsoleCommand(String[] cmd){
		if (cmd[0].equals("onlotto")){
			if(cmd.length > 1){
				if (cmd[1].equals("time")){
					return OLA.ConsoledisplayTimeTill();
				}else if (cmd[1].equals("broadcast")){
					return OLA.broadcastTimeTill();
				}else if (cmd[1].equals("draw")){
					return OLA.drawNOW();
				}
			}
		}
		return false;
	}
}
