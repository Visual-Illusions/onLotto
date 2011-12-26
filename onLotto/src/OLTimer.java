import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

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
