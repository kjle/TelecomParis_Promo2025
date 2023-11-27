package client;

public class HelloClientGraphic extends HelloClient{

	private static final long serialVersionUID = 1L;
	
	DisplayFrame displayFrame;
	
	public HelloClientGraphic(String host, int port) {
		
		super(host, port);
		this.displayFrame = new DisplayFrame(this.message);

	}
	
	public static void main (String args[]) {
		if (args.length != 2) {
			System.out.println ("Deux arguments : 1) port-rmiregistry; 2) machine ");
			System.exit (2);
		}

		//the port on which the remote rmiregistry is listening 
		int portRmiregistry = new Integer(args[0]).intValue();
		
		//the host on which the remote server object and the rmitregistry are executing
		String remoteHost = args[1];
		
		//instantiate a HelloClient that displays server messages graphically
		new HelloClientGraphic(remoteHost, portRmiregistry);
	}
	




}
