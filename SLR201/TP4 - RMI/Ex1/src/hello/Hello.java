package hello;

/**
 * the remote object's interface
 *  
 * this interface must be public and must implement java.rmi.Remote
 * all remote methods must throw java.rmi.RemoteException
 */
public interface Hello extends java.rmi.Remote {
	
	/**
	 * returns a message to (remote) clients
	 * @return a message
	 * @throws java.rmi.RemoteException
	 */
	String readMessage() throws java.rmi.RemoteException;
}


