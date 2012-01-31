import java.util.Timer;
import java.util.TimerTask;
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

public class OLTimer{
	Logger log = Logger.getLogger("Minecraft");
	OLActions OLA;
	OLData OLD;
	
	long Delay;
	long Reset;
	
	Timer RunTime;
	
	public OLTimer(OLActions OLA, OLData OLD){
		this.OLA = OLA;
		this.OLD = OLD;
	}
	
	public void startTimer(){
		Delay = OLD.Delay;
		Reset = OLD.Reset;
		
		if(Reset < 1){
			Reset = Delay;
		}
		else{
			Reset -= System.currentTimeMillis();
			if(Reset < 1){
				Reset = 60000;
			}
		}
		RunTime = new Timer();
		RunTime.schedule(new DrawLotto(), Reset);
	}
	
	public void RestartTimer(){
		RunTime.cancel();
		RunTime.purge();
		RunTime = new Timer();
		OLD.saveReset(Delay+System.currentTimeMillis());
		RunTime.schedule(new DrawLotto(), Delay);
	}
	
	public void cancelTimer(){
		RunTime.cancel();
		RunTime.purge();
	}
	
	
	public class DrawLotto extends TimerTask{
		public void run(){
			OLA.drawLotto();
			OLD.saveReset(Delay+System.currentTimeMillis());
			RunTime.schedule(new DrawLotto(), Delay);
		}
	}
}
