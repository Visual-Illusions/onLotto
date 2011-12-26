import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OLActions {
	Server server = etc.getServer();
	OLData OLD;
	OLListener OLL;
	boolean drawing;
	
	public OLActions(OLData OLD, OLListener OLL){
		this.OLD = OLD;
		this.OLL = OLL;
	}
	
	public void drawLotto(){
		drawing = true;
		List<Player> Tickets = new ArrayList<Player>();
		Tickets.addAll(etc.getServer().getPlayerList());
		for(int i = 0; i < Tickets.size(); i++){
			Player CPC = Tickets.get(i);
			if(!OLD.IACP && CPC.isAdmin()){
				Tickets.remove(CPC);
			}
			if(!OLD.HICCP && CanUseGive(CPC)){
				Tickets.remove(CPC);
			}
		}
		if(Tickets.isEmpty()) {
			server.messageAll("§2[§donLotto§2]§c No one online to play. Starting new round!");
			OLD.log.info("[onLotto] No one online who can play. Starting new round!");
			drawing = false;
		}
		else if(Tickets.size() < OLD.MPO){
			server.messageAll("[§dILotto§f]§c Not enough players online. Starting new round!");
			OLD.log.info("[onLotto] Not enough player online. Starting new round!");
			drawing = false;
		}
		else{
			int rand = new Random().nextInt(Tickets.size());
			Player player = Tickets.get(rand);
			String name = player.getName();
			Item WinItem = null;
			if (!OLD.RandItem){
				addItem(player.getInventory(), OLD.WinItem.getItemId(), OLD.WinItem.getDamage(), OLD.WinItem.getAmount());
				WinItem = OLD.WinItem;
			}
			else{
				List<Item> RandItems = OLD.getRandItemList();
				int randI = new Random().nextInt(RandItems.size());
				WinItem = RandItems.get(randI);
				addItem(player.getInventory(), WinItem.getItemId(), WinItem.getDamage(), WinItem.getAmount());
			}
			String Itemname = WinItem.itemType.name();
			if(Itemname == null){
				Itemname = String.valueOf(WinItem.getItemId());
			}
			server.messageAll("§2[§donLotto§2]§b Congratulations to §e" + name);
			server.messageAll("§2[§donLotto§2]§b for winning §e" + WinItem.getAmount() + " " + Itemname);
			server.messageAll("§2[§donLotto§2]§b There was in total §e" + Tickets.size() + " §bplayers online eligble to win.");
			OLD.log.info("[onLotto] Winner was " + name + ". Item won was "+WinItem.getAmount()+" "+Itemname+" Starting new round!");
			if (OLD.UTE && (etc.getLoader().getPlugin("TwitterEvents") != null) && (etc.getLoader().getPlugin("TwitterEvents").isEnabled())){
				etc.getLoader().callCustomHook("tweet", new Object[] { OLD.Tweet.replace("<P>", name).replace("<A>", String.valueOf(WinItem.getAmount()).replace("<I>", Itemname))});
			}
			drawing = false;
		}
	}
	
	public boolean drawNOW(){
		drawLotto();
		OLL.OLT.RestartTimer();
		return true;
	}
	
	public boolean displayTimeTill(Player player){
		player.sendMessage("§2[§donLotto§2]§b Pulling winner in:");
		player.sendMessage("§e"+timeUntil(OLD.Reset));
		return true;
	}
	
	public boolean ConsoledisplayTimeTill(){
		OLD.log.info("[onLotto] Pulling winner in:");
		OLD.log.info(timeUntil(OLD.Reset));
		return true;
	}
	
	public boolean broadcastTimeTill(){
		server.messageAll("§2[§donLotto§2]§b Pulling winner in:");
		server.messageAll("§e"+timeUntil(OLD.Reset));
		OLD.log.info("[onLotto] Pulling winner in:");
		OLD.log.info(timeUntil(OLD.Reset));
		return true;
	}
	
	public String timeUntil(long time) {
		if(!drawing){
			double timeLeft = Double.parseDouble(Long.toString(((time - System.currentTimeMillis()) / 1000)));
			StringBuffer Time = new StringBuffer();
			if(timeLeft >= 60 * 60 * 24) {
				int days = (int) Math.floor(timeLeft / (60 * 60 * 24));
				timeLeft -= 60 * 60 * 24 * days;
				if(days == 1) {
					Time.append(days + " day, ");
				} 
				else{
					Time.append(days + " days, ");
				}
			}
			if(timeLeft >= 60 * 60) {
				int hours = (int) Math.floor(timeLeft / (60 * 60));
				timeLeft -= 60 * 60 * hours;
				if(hours == 1) {
					Time.append(hours + " hour, ");
				} else {
					Time.append(hours + " hours, ");
				}
			}
			if(timeLeft >= 60) {
				int minutes = (int) Math.floor(timeLeft / (60));
				timeLeft -= 60 * minutes;
				if(minutes == 1) {
					Time.append(minutes + " minute ");
				} else {
					Time.append(minutes + " minutes ");
				}
			}
			int secs = (int) timeLeft;
			if(Time != null) {
				Time.append("and ");
			}
			if(secs == 1) {
				Time.append(secs + " second.");
			}
			else if(secs > -1){
				Time.append(secs + " seconds.");
			}
			else{
				Time = new StringBuffer();
				Time.append("Derp (manual draw lotto)");
			}
			return Time.toString();
		}
		else{
			return "NOW!";
		}
	}
	
	public void addItem(Inventory inv, int ID, int Damage, int amount){
		for (int i = 0; i < inv.getContentsSize(); i++){
			if (amount > 0){
				Item item = inv.getItemFromSlot(i);
				if (item != null){
					if (item.getItemId() == ID){
						if (item.getDamage() == Damage){
							if (amount > 64){
								if(item.getAmount() < 64){
									inv.removeItem(i);
									inv.setSlot(ID, 64, Damage, i);
									inv.update();
									amount -= item.getAmount();
								}
								else{
									continue;
								}
							}
							else{
								if(item.getAmount() < 64){
									inv.removeItem(i);
									inv.setSlot(ID, amount+item.getAmount(), Damage, i);
									inv.update();
									amount -= item.getAmount();
								}
								else{
									continue;
								}
							}
						}
					}
				}
				else{
					if (amount > 64){
						inv.setSlot(ID, 64, Damage, i);
						inv.update();
						amount -= 64;
					}
					else{
						inv.setSlot(ID, amount, Damage, i);
						inv.update();
						break;
					}
				}
			}
			else{
				break;
			}
		}
		return;
	}
	
	public boolean CanUseGive(Player player){
		if((player.canUseCommand("/give")) || (player.canUseCommand("/i")) || (player.canUseCommand("/item"))){
			return true;
		}
		return false;
	}
}
