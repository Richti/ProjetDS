package application;

import application.clients.ClientA;
import application.clients.ClientB;
import application.clients.ClientC;
import application.servers.ServerA;
import application.servers.ServerB;
import application.servers.ServerC;
import framework.registries.GlobalRegistry;

// 
public class Launcher {
	  public static void main(String[] args) throws Exception {
		 
		  Thread l = new LauncherGR();
		  l.start();
		   
		  ServerB.main(null);
		  ServerA.main(null);
		  ServerC.main(null);
		  
		  ClientC.main(null);
		  ClientB.main(null);
		  ClientA.main(null);
		  
	  }
	  
	  // The GlobalRegistry must be launched in a thread 
	  static class LauncherGR extends Thread {
		  
		  @Override
		  public void run(){
			  try {
				GlobalRegistry.main(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		  }
	  }

}
