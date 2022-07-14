package edu.du.cs.mahnsmcgee.painter;

import java.io.*;
import java.net.*;
import java.util.*;


public class PainterThread implements Runnable {
	private Socket client;
	private Hub hub;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	private String grader;
	PainterThread pt;

	public PainterThread(Socket client) {
		this.client = client;
		//  this.Hub = Hub;
		pt = this;

		try {
			this.ois = new ObjectInputStream(client.getInputStream());
			this.oos = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("PAINTERTHREAD ERROR: oos/ois init failure");
			Hub.broadcastDisconnect(pt);
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			Hub.painters.add(this);

			//Enter grader as your name on the prompt
			this.grader = (String) ois.readObject();


			Hub.broadcastMessage("---------" + this.grader + " has entered the studio ---------");
			System.out.println("PainterThread:\t grader: " + this.grader);

			try {
				Thread canvasListener = new Thread() {
					public void run() {
						try {
							while(true) {
								Object obj = ois.readObject();

								if(obj.getClass().toString().contains("String")) {
									String toBroadcast = (String) obj;
									Hub.broadcastMessage(toBroadcast);
								}else if(obj.getClass().toString().contains("ArrrayList")){

								}else {
									PaintingPrimitive toBroadcast = (PaintingPrimitive) obj;
									Hub.broadcastShape(toBroadcast);
								}
							}
						}catch(ClassNotFoundException e) {
							Hub.broadcastDisconnect(pt);
							Hub.broadcastMessage("---------" + pt.getUsername() + " has left the studio ---------");
							//System.out.println("PAINTERTHREAD ERROR: canvasListener failure");
							//e.printStackTrace();
						} catch (EOFException eof) {
							Hub.broadcastDisconnect(pt);
							Hub.broadcastMessage("---------" + pt.getUsername() + " has left the studio ---------");
							//System.out.println("PAINTERTHREAD ERROR: canvasListener EOF");
							//eof.printStackTrace();
						}catch (IOException e) {
							Hub.broadcastDisconnect(pt);
							Hub.broadcastMessage("---------" + pt.getUsername() + " has left the studio ---------");
							//e.printStackTrace();
						}
					}
				};
				canvasListener.start();
			}catch(Exception e) {
				Hub.broadcastDisconnect(pt);
				System.out.println("PAINTERTHREAD ERROR: Unknown Exception");
				e.printStackTrace();
			}
		}catch(ClassNotFoundException e) {
			Hub.broadcastDisconnect(pt);
			System.out.println("PAINTERTHREAD ERROR: run() ClassNotFound failure");
			e.printStackTrace();
		} catch (IOException e1) {
			Hub.broadcastDisconnect(pt);
			e1.printStackTrace();
		}
	}

	public synchronized void initPainterPanel(ArrayList<PaintingPrimitive> canvas) {
		System.out.println("PainterThread: Init canvas");
		try {
			synchronized(this) {
				oos.writeObject(canvas);
			}
		} catch (IOException e) {
			Hub.broadcastDisconnect(pt);
			System.out.println("PAINTERTHREAD ERROR: canvas init failure");
			e.printStackTrace();
		}
	}

	public synchronized void chatUpdateFromHub(String message) {
		try {
			oos.writeObject(message);
		} catch (IOException e) {
			Hub.broadcastDisconnect(pt);
			System.out.println("PainterThread ERROR: chatUpdateFromHub failure");
			e.printStackTrace();
			System.exit(0);
		}
	}
	public synchronized void shapeUpdateFromHub(PaintingPrimitive shape) {
		try {
			oos.writeObject(shape);
		} catch (IOException e) {
			Hub.broadcastDisconnect(pt);
			System.out.println("PainterThread ERROR: shapeUpdateFromHub failure");
			e.printStackTrace();
			System.exit(0);
		}

	}

	public synchronized void updateFromPainter(ObjectInputStream ois) {
		try {
			Object obj = ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public String getUsername() {
		return this.grader;
	}

	public String toString() {
		return "Controller for: " + this.grader;
	}
}

