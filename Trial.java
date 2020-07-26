///////////   IMPORT FILES      //////////////

import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
//import java.io.BufferedReader; 
//import java.io.IOException; 
//import java.io.InputStreamReader;
import java.util.Scanner;







/////////        CLASS SYNCCLASS       ////////////////

class SyncClass {
   public int contents = -1;
   public int available = 0;
   public int max;
   private static SyncClass obj;
   private SyncClass(int max) {
	   this.max = max;
   }
   public static synchronized SyncClass getInstance(int max) { 
       if (obj==null) 
           obj = new SyncClass(max); 
       return obj; 
   }
   public void decrement() {
	   available = available - 1;
   }
   public synchronized int get(int play_no) {
      while (available != play_no) {
         try {
            wait();
         } catch (InterruptedException e) {}
      }
      return contents;
   }
   public synchronized void put(int value) {
      while (available != 0) {
         try {
            wait();
         } catch (InterruptedException e) { } 
      }
      contents = value;
      available = max;
   }
   public synchronized void notice() {
	   notifyAll();
   }
}







////////////       CLASS PLAYER        ////////////

class Player extends Thread {
   public SyncClass sync_obj;
   public int num;
   public int card[];
   public int strike = 0;
   public boolean can_print = true;
   public Player(SyncClass c, int numb, int arr[]) {
      sync_obj = c;
      this.num = numb;
      card = arr;
   }
   public void printArr() {
	   for (int element: this.card) {
           System.out.print(element);
           System.out.print(", ");
       }
   }
   public void init_card() {
	   for(int i = 0; i < 10; i++) {
		   this.card[i] = (int)(Math.random()*50);
	   }
   }
   public boolean check(int numb) {
	   boolean avail = false;
	   for(int i = 0; i < 10; i++) {
		   if(card[i] == numb) {
			   card[i] = -1;
			   strike = strike + 1;
			   avail = true;
			   break;
		   }
	   }
	   return avail;
   }
   public void run() {
      int value = 0;
      boolean avail = false;
      for (int i = 0; i < 10; i++) {
         value = sync_obj.get(num);
         avail = check(value);
         if(avail) {
        	 System.out.println("// Player #" + this.num + " got: " + value);
         }
         if(strike!=3) {
        	 sync_obj.decrement();
        	 sync_obj.notice();
         }
         else {
        	 //STOP ALL THREADS
        	 sync_obj.available = -1;
        	 if(can_print) {
        		 can_print = false;
        		 System.out.println("\n*** Player #" + this.num + " WINS! ***");
        	 }
        	 
         }
      }
   }
}




////////       CLASS MODERATOR      /////////////

class Moderator<T> extends Thread {
   public SyncClass sync_obj;
   public T number;
   public Moderator(SyncClass c, T number) {
      sync_obj = c;
      this.number = number;
   } 
   public void run() {
	  int val = 0;
	  Queue<Integer> q = new LinkedList<>();
	  for (int k = 0; k < 10; k++) {
		  val = (int)(Math.random() * 50);
		  q.add(val);
	  }
      for (int i = 0; i < 10; i++) {
    	 try {
    		 val = q.remove();
    	 } catch (NoSuchElementException n) {}
    	  
         sync_obj.put(val);
         System.out.println("Moderator puts value no "+ (i+1) +": " + val);
         sync_obj.notice();
         try {
            sleep(1000);
         } catch (InterruptedException e) { }
      } 
   }
}
	




/////////         CLASS TRIAL      ///////////////////

public class Trial {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner in = new Scanner(System.in);
		System.out.print("Enter the no of players: ");
		int a = 2;
		try {
			a = in.nextInt();
		} catch (InputMismatchException ime) {
			System.out.println("Input Mismatch Exception occured!");
			System.out.println("Initialising default value of 2\n");
			a = 2;
		}
		if(a<2) {
			System.out.println("Can't keep the no of players less than 2!");
			a = 2;
		}
		
		int[][] intArray = new int[a][10];
		SyncClass c = SyncClass.getInstance(a);
	    Moderator<Integer> p1 = new Moderator<Integer>(c, 1);
	    Player con[] = new Player[a];
	    
	    for(int i=0; i<a; i++) {
	    	con[i] = new Player(c,i+1,intArray[i]);
	    }
	    
	    for(int j=0; j<a; j++) {
	    	con[j].init_card();
	    }
	    
	    for(int k = 0; k<a; k++) {
	    	System.out.print("Player "+ (k+1) +" Card:  ");
	    	con[k].printArr();
	    	System.out.print("\n");
	    }

	    System.out.println("\n");
	    p1.start(); 
	    for(int l=0; l<a; l++) {
	    	con[l].start();
	    }

	    in.close();
	}

}



