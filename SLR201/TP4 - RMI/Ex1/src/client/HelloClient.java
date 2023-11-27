package client;

import hello.Hello;

//import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/**
 * the client that will connect to the remote server object (HelloServer), 
 * 		request a message from it and display the message received 
 * 
 * --> by default, the message will be displayed as text in the output console; 
 * --> use HelloClientGraphic instead for displaying the result graphically (in a GUI window) 
 */
public class HelloClient {

	//attribute storing the message from the server
	String message = "";
	
	//the name of the hello service to use
	static final String HELLO_SERVICE_NAME = "HelloService";

	
	public HelloClient (String host, int port) {
		try {
			//if there is no security manager then create one
			if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new SecurityManager());
	        }
			//get a reference to the remote object Registry on the specified host and port.
			Registry registry = LocateRegistry.getRegistry(host, port);
			
			//lookup the hello service in the rmiregistry using its service name
			//obtain a reference to the remote server object
			//this reference will be used later to call methods on the remote object (e.g. readMessage())
			Hello hello = (Hello)registry.lookup(HELLO_SERVICE_NAME);
			
			System.out.println("Connection to service: " + HELLO_SERVICE_NAME + "; on host: " + host);

			//use the remote object's reference to call methods on this object 
			//the remote object executes on the server host 
			this.message = hello.readMessage ();
			
			//print the message
			System.out.println("Message from remote HelloService: " + this.message);
		}

		catch (Exception e) {
			System.out.println ("Hello exception: " + e.getMessage ());
			e.printStackTrace ();
		}

	}

	/**
	 * main method: check input parameters and create a HelloClient object
	 * 
	 * @param args: two input parameters expected: 
	 * 	1. the listening port of the rmiregistry executing on the server 
	 * 	2. the server's host name 
	 */
	public static void main (String args[]) {
		if (args.length != 2) {
			System.out.println ("Deux arguments : 1) port-rmiregistry; 2) machine ");
			System.exit (2);
		}
		// Port permettant d'acceder a rmiregistry
		//
		int portRmiregistry = new Integer(args[0]).intValue();
		// Machine du rmiregistry du serveur
		String remoteHost = args[1];

		new HelloClient(remoteHost, portRmiregistry);
	}



}
