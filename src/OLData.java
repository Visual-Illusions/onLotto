import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

public class OLData {
	Logger log = Logger.getLogger("Minecraft");
	PluginLoader Loader = etc.getLoader();
	
	List<Item> RandItemList;
	Item WinItem = new Item(264, 1, -1, 0);
	
	boolean RandItem = false, UTE = true, HICCP = true, IACP = true;
	String Tweet = "<P> won <A> of <I> from onLotto!";
	int MPO = 1;
	
	long Reset = 0, Delay = 60;
	
	PropertiesFile Reseter;
	PropertiesFile PropProp;
	
	String Dire = "plugins/config/onLotto/";
	String Props = "plugins/config/onLotto/onLottoProperties.ini";
	String RandItemsFile = "plugins/config/onLotto/onLottoRandomItem.txt";
	String ReseterFile = "plugins/config/onLotto/onLottoTimer.DONOTEDIT";
	
	public OLData(){
		RandItemList = new ArrayList<Item>();
		LoadSettings();
	}
	
	public void LoadSettings(){
		File PropFile = new File(Props);
		File DireDir = new File(Dire);
		File RandItemFile = new File(RandItemsFile);
		if(!DireDir.exists()){
			DireDir.mkdirs();
		}
		if(!RandItemFile.exists()){
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(RandItemFile));
				out.write("# Format ID,Amount,Damage (Amount and Damage optional [will default to Amount 1 Damage 0])#"); out.newLine();
				out.write("1,1,0"); out.newLine();
				out.write("35,2,2"); out.newLine();
				out.write("35,2,1"); out.newLine();
				out.write("35,2,0"); out.newLine();
				out.close();
			} catch (IOException e) {
				this.log.severe("[onLotto] - Unable to save players with tickets");
			}
		}
		if(!PropFile.exists()){
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(PropFile));
				out.write("##### ItemLottery Properties File #####"); out.newLine();
				out.write("#Use TwitterEvents (Requires TwitterEvents Plugin)#"); out.newLine();
				out.write("Use-TwitterEvents="+UTE); out.newLine();
				out.write("#Use Random Items#"); out.newLine();
				out.write("Use-RandomItems="+RandItem); out.newLine();
				out.write("#Win Item (if Random Items isn't used) Format ID,Amount,Damage (Amount and Damage optional [will default to Amount 1 Damage 0])#"); out.newLine();
				out.write("WinItem=264,1,0"); out.newLine();
				out.write("#Draw Delay (in minutes)#"); out.newLine();
				out.write("DrawDelay="+Delay); out.newLine();
				out.write("#Minimum Number of OnLine Players#"); out.newLine();
				out.write("MinimumPlayersOn="+MPO); out.newLine();
				out.write("#Allow Those with /i /item or /give to play#"); out.newLine();
				out.write("HasItemCommandCanPlay="+HICCP); out.newLine();
				out.write("#Allow Admins to player#"); out.newLine();
				out.write("IsAdminCanPlay="+IACP); out.newLine();
				out.close();
			} catch (IOException e) {
				log.severe("[onLotto] - Unable to create Properties File");
			}
			Delay *= 60000;
		}
		else{
			PropProp = new PropertiesFile(Props);
			Reseter = new PropertiesFile(ReseterFile);
			RandItem = PropProp.getBoolean("Use-RandomItems");
			if(!RandItem){
				String ItemWin = PropProp.getString("WinItem");
				String[] ItemSplit = ItemWin.split(",");
				int ID = 264, Amount = 1, Damage = 0;
				if(ItemSplit != null && ItemSplit.length > 1){
					try{
						ID = Integer.parseInt(ItemSplit[0]);
					}catch(NumberFormatException NFE){
						log.severe("[onLotto] There was an issue with WinItem ID. Defaulting to Diamond!");
						ID = 264;
					}
					if(ID < 1){
						log.severe("[onLotto] There was an issue with WinItem 'ID'. Defaulting to Diamond!");
						ID = 264;
					}
					if(!Item.isValidItem(ID)){
						log.severe("[onLotto] There was an issue with WinItem 'ID'. Defaulting to Diamond!");
						ID = 264;
					}
					try{
						Amount = Integer.parseInt(ItemSplit[1]);
					}catch(NumberFormatException NFE){
						log.severe("[onLotto] There was an issue with WinItem 'Amount'. Defaulting to 1!");
						Amount = 1;
					}
					if(Amount < 1){
						log.severe("[onLotto] There was an issue with WinItem 'Amount'. Defaulting to 1!");
						Amount = 1;
					}
					if(ItemSplit.length > 2){
						try{
							Damage = Integer.parseInt(ItemSplit[2]);
						}catch(NumberFormatException NFE){
							log.severe("[onLotto] There was an issue with WinItem 'Damage'. Defaulting to 0!");
							Damage = 0;
						}
						if(Damage < 0){
							log.severe("[onLotto] There was an issue with WinItem 'Damage'. Defaulting to 0!");
							Damage = 0;
						}
					}
				}
				else{
					try{
						ID = Integer.parseInt(ItemWin);
					}catch(NumberFormatException NFE){
						log.severe("[onLotto] There was an issue with WinItem 'ID'. Defaulting to Diamond!");
						ID = 264;
					}
					if(ID < 0){
						log.severe("[onLotto] There was an issue with WinItem 'ID'. Defaulting to Diamond!");
						ID = 264;
					}
					if(!Item.isValidItem(ID)){
						log.severe("[onLotto] There was an issue with WinItem. Defaulting to Item:Diamond Amount: 1!");
					}
				}
				WinItem = new Item(ID, Amount, -1, Damage);
			}
			else{
				PopulateRandItem();
			}
			Delay = PropProp.getLong("DrawDelay")*60000;
			Reset = Reseter.getLong("TimerResetTo", 0);
			UTE = PropProp.getBoolean("Use-TwitterEvents");
			MPO = PropProp.getInt("MinimumPlayersOn");
			HICCP = PropProp.getBoolean("HasItemCommandCanPlay");
			IACP = PropProp.getBoolean("IsAdminCanPlay");
		}
	}
	
	public void PopulateRandItem(){
		try {
		    BufferedReader in = new BufferedReader(new FileReader(RandItemsFile));
		    String str;
		    int line = 1;
		    while ((str = in.readLine()) != null) {
		    	if(!str.contains("#")){
		    		String[] item = str.split(",");
		    		int ID = 264, Amount = 1, Damage = 0;
		    		if(item != null && item.length > 1){
		    			try{
		    				ID = Integer.parseInt(item[0]);
		    			}catch(NumberFormatException NFE){
		    				log.severe("[onLotto] There was an issue with RandItem 'ID' at line:"+line+".");
		    				continue;
		    			}
		    			if(ID < 1){
		    				log.severe("[onLotto] There was an issue with RandItem 'ID' at line:"+line+".");
		    				continue;
						}
						if(!Item.isValidItem(ID)){
							log.severe("[onLotto] There was an issue with RandItem 'ID' at line:"+line+".");
		    				continue;
						}
						try{
							Amount = Integer.parseInt(item[1]);
						}catch(NumberFormatException NFE){
							log.severe("[onLotto] There was an issue with RandItem 'Amount' at line:"+line+".");
		    				continue;
						}
						if(Amount < 1){
							log.severe("[onLotto] There was an issue with RandItem 'Amount' at line:"+line+".");
		    				continue;
						}
						if(item.length > 2){
							try{
								Damage = Integer.parseInt(item[2]);
							}catch(NumberFormatException NFE){
								log.severe("[onLotto] There was an issue with RandItem 'Damage' at line:"+line+".");
			    				continue;
							}
							if(Damage < 0){
								log.severe("[onLotto] There was an issue with RandItem 'Damage' at line:"+line+".");
			    				continue;
							}
						}
					}
					else{
						try{
							ID = Integer.parseInt(str);
						}catch(NumberFormatException NFE){
							log.severe("[onLotto] There was an issue with RandItem 'ID' at line:"+line+".");
		    				continue;
						}
						if(ID < 0){
							log.severe("[onLotto] There was an issue with RandItem 'ID' at line:"+line+".");
		    				continue;
						}
						if(!Item.isValidItem(ID)){
							log.severe("[onLotto] There was an issue with RandItem 'ID' at line:"+line+".");
		    				continue;
						}
					}
		    		Item randItem = new Item(ID, Amount, -1, Damage);
		    		RandItemList.add(randItem);
		    		line++;
		    	}	
		    }
		    in.close();
		}catch (IOException e){
			log.severe("[onLotto] - Unable to load RandItemFile - Using default Item");
			RandItem = false;
		}
		if(RandItemList.isEmpty()){
			log.severe("[onLotto] There were no Item in the RandItemFile. - Using default Item");
			RandItem = false;
		}
		
	}
	
	public List<Item> getRandItemList(){
		return RandItemList;
	}
	
	public void saveReset(long reset){
		Reset = reset;
		Reseter.setLong("TimerResetTo", reset);
	}
}
