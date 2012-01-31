
/**
* onLotto v1.x
* Copyright (C) 2012 Visual Illusions Entertainment
* @author darkdiplomat <darkdiplomat@visualillusionsent.net>
* 
* This file is part of onLotto
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see http://www.gnu.org/copyleft/gpl.html.
*/

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
