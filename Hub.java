package edu.du.cs.mahnsmcgee.painter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class Hub {

   static HashSet<PainterThread> painters = new HashSet<PainterThread>();
   static ArrayList<PaintingPrimitive> masterCanvas = new ArrayList<PaintingPrimitive>();

   public static void broadcastMessage(String message) {
      for(PainterThread pt : painters) {
         pt.chatUpdateFromHub(message + "\n");
      }
   }

   public static void broadcastShape(PaintingPrimitive shape) {
	  masterCanvas.add(shape);
   
   	//since a shape cannot be deleted, only need to send the most recent shape to each Painter
      for(PainterThread pt : painters) {
         try {
            pt.shapeUpdateFromHub(shape);
         }catch(Exception e) {
        	broadcastDisconnect(pt);
            System.out.println("HUB ERROR: shapeUpdateFromHub");
            e.printStackTrace();
            System.exit(0);
         }
      }
   }
   public static void broadcastDisconnect(PainterThread pt) {
	  System.out.println("Painter " + pt.getUsername() + " has disconnected.");
      painters.remove(pt);
   }
   private void startHub() {
      System.out.println("Hub started, awaiting Painter connections...");
      ServerSocket ss = null;
      Socket s = null;
      PainterThread pt = null;
      try {
         ss = new ServerSocket(7005);
         while(true) {
            s = ss.accept();
            pt = new PainterThread(s);
            Thread thread = new Thread(pt);
            painters.add(pt);
            for(PaintingPrimitive p : masterCanvas) {
               pt.shapeUpdateFromHub(p);
            }
            thread.start();
         }
      }catch(IOException e) {
    	 broadcastDisconnect(pt);
         e.printStackTrace();
         System.exit(0);
      }
   }

   public static void main(String[] args) {
      new Hub().startHub();
   }
}

