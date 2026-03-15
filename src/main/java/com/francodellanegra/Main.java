package com.francodellanegra;

import javax.swing.SwingUtilities;

import controller.ClockOrchestrator;
import controller.connectionMakerAndControlUnit.*;
import view.CitySelector;
import view.MainUIInitializer;


public class Main 
{
	

	private static void run() {
		
		CitySelector.start();
    	
    	ClockOrchestrator.start();
    	
    	
    	MainUIInitializer.showConnectionError(MasterConnectionStatusChecker.getSingleton().isConnectionActive());	
	
    	
        SwingUtilities.invokeLater(() -> {
            MainUIInitializer.showLoadingScreen(() -> {
                SwingUtilities.invokeLater(() -> {
					try {
						MainUIInitializer.start();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
            });
        });
    	
    	
		
	}
	
	
    public static void main( String[] args ) throws InterruptedException
    {    	
    	Main.run();
    }
}
