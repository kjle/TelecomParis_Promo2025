package client;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DisplayFrame extends Frame{

	private static final long serialVersionUID = 1L;

	String message;

	public DisplayFrame(String message){
		
		this.message = message;
		
		//close the window when left-clicking on the X button
		this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent we){
                System.exit(0);
             }
        });
		
		//display the result obtained from the server
		setSize (500, 120);
		setVisible (true);
	}
	
	//paint the frame's contents
	//called *automatically* in reaction to internal/external events (managed by the java GUI) 
	//  - e.g. when the window become visible for the first time; 
	//	or, when parts of the window become visible after having been hidden behind another window; ...
	public void paint (Graphics g) {
		g.drawString (message, 25, 50);
	}
}
