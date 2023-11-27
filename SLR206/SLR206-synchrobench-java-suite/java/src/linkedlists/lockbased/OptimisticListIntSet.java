package linkedlists.lockbased;

/*
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
*/

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import contention.abstractions.AbstractCompositionalIntSet;

public class OptimisticListIntSet extends AbstractCompositionalIntSet {


    private Node head;
  

    public OptimisticListIntSet(){     
		head = new Node(Integer.MIN_VALUE);
          head.next = new Node(Integer.MAX_VALUE);
    }


private boolean validate(Node pred, Node curr) {
    Node node=head;
    while (node.key <= pred.key){
 	       if (node==pred){
		 return pred.next==curr;}
	       node=node.next;
	 }      	
	 return false;
  }




   public boolean addInt(int item){	
   while (true){
      Node pred=head;
      Node curr=pred.next;
	   while (curr.key<item){
         pred=curr;
	      curr=curr.next;
	   }
      pred.lock(); curr.lock();
	   try {
	      if (validate(pred,curr)){
	        if (curr.key==item) {	 return false;
		}
	        Node node = new Node(item);
 	        node.next=curr;
	        pred.next=node;	    
	        return true; }
	   } finally{
	      pred.unlock();
	      curr.unlock();}
   }
   }
        
    public boolean removeInt(int item) {	
   while (true){
      Node pred=head;
      Node curr=pred.next;
	   while (curr.key<item){
         pred=curr;
	      curr=curr.next;
	   }
      pred.lock(); curr.lock();
	   try {
	      if (validate(pred,curr)){
	        if (curr.key==item) {	 	
		    pred.next=curr.next; 	 	
		    return true;
		}
	        return false; }
	   } finally{
	      pred.unlock();
	      curr.unlock();
      } 
   }
    }


public boolean containsInt(int item) { 
	while (true){
      Node pred=head;
      Node curr=pred.next;
	   while (curr.key<item){
         pred=curr;
	      curr=curr.next;
	   }
      pred.lock(); curr.lock();
	   try {
	      if (validate(pred,curr)){
	        	return (curr.key==item);
		} 	   
      } finally{
	      pred.unlock();
	      curr.unlock();}
      }
	} 



    /**
         * list Node
         */
        private class Node {
               
      
	    public int key;
	    public Node next;
            Lock lock;

                /**
                 * Constructor for usual Node
                 * @param item element in list
                 */
                Node(int item) {      // usual constructor
                        this.key = item;
                        this.next = null;
                        this.lock = new ReentrantLock();
                }
 
                /**
                 * Lock Node
                 */
                void lock() {lock.lock();}
               
                /**
                 * Unlock Node
                 */
                void unlock() {lock.unlock();}
        }


	@Override
	public int size() { 
	int n = 0; 
        Node node = head;

        while (node.next.key < Integer.MAX_VALUE) {
            n++;
            node = node.next;
	    } 
        return n;
    }
	
	
	@Override
	    public void clear() { 
	    Node max = new Node(Integer.MAX_VALUE);
	    head.next = max; 
		
	}



}
