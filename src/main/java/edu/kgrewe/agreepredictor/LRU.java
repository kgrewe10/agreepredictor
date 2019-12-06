package edu.kgrewe.agreepredictor;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class LRU { 
    // store keys of cache 
    private Deque<String> dq; 
    // store references of key in cache 
    private HashSet<String> map; 
    // maximum capacity of cache 
    private int csize; 
  
    LRU(int n) 
    { 
        dq = new LinkedList<String>(); 
        map = new HashSet<String>(); 
        csize = n; 
    } 
  
    /* Refers key x with in the LRU cache */
    public void access(String x) 
    { 
        if (!map.contains(x)) { 
            if (dq.size() == csize) { 
                String last = dq.removeLast(); 
                map.remove(last); 
            } 
        } 
        else { 
            /* The found page may not be always the last element, even if it's an  
               intermediate element that needs to be removed and added to the start  
               of the Queue */
            int index = 0, i = 0; 
            Iterator<String> itr = dq.iterator(); 
            while (itr.hasNext()) { 
                if (itr.next().equals(x)) { 
                    index = i; 
                    break; 
                } 
                i++; 
            } 
            dq.remove(x); 
        } 
        dq.push(x); 
        map.add(x); 
    } 
  
    // display contents of cache 
    public void display() 
    { 
    	System.out.println("\n-----------LRU--------------\n");
        Iterator<String> itr = dq.iterator(); 
        while (itr.hasNext()) { 
            System.out.print(itr.next() + " "); 
        } 
        
        System.out.println("\n");
    } 
    
    public String getLRU() {
    	if(dq.size() < 1) {
    		return null;
    	}
    	return dq.getLast();
    }
    
    public int getSize() {
    	return dq.size();
    }

} 
