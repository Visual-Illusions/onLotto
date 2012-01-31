import java.util.logging.Logger;

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

public class onLotto extends Plugin {
	Logger log = Logger.getLogger("Minecraft");
	OLListener OLL;
	OLData OLD;
	String name = "onLotto";
	String version = "1.1";
	String author = "darkdiplomat";
	
	public void disable(){
		OLL.OLT.cancelTimer();
		etc.getInstance().removeCommand("/onlotto");
		log.info(name + " version " + version + " disabled!");
	}
	
	public void enable() {
		etc.getInstance().addCommand("/onlotto", " - display info for onLotto");
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
