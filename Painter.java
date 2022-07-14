package edu.du.cs.mahnsmcgee.painter;

import java.io.*;
import java.net.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Painter extends JFrame implements MouseListener, MouseMotionListener, 
ActionListener, KeyListener, FocusListener{

	//JFrame setup
	private final int FRAME_WIDTH = 500;
	private final int FRAME_HEIGHT = 500;

	private PaintingPrimitive shapeToDraw;
	private PaintingPanel paintPanel;
	private JPanel chat;
	private JTextArea msgInput;
	private JTextArea feed;

	//drawing instance variables
	private Color color = Color.BLUE;
	private String shape = "circle"; 
	private Point startPoint;

	static ObjectOutputStream oos;
	static ObjectInputStream ois;

	public Painter(String grader) {
		super(grader);
		try {
			Socket s = new Socket("localhost", 7005);
			{
				oos = new ObjectOutputStream(s.getOutputStream());
				ois = new ObjectInputStream(s.getInputStream());
			};
			//ask the painter for name
			oos.writeObject(grader);
		} catch (IOException e) {
			System.out.println("Enter grader: ");
			e.printStackTrace();
			System.exit(0);
		}

		setSize(FRAME_WIDTH,FRAME_HEIGHT);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel holder = new JPanel();

		holder.setLayout(new BorderLayout());

		// Creating  paints 
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(3, 1)); // 3 by 1

		// Red
		JButton redPaint = new JButton();
		redPaint.setBackground(Color.RED);
		redPaint.setOpaque(true);
		redPaint.setBorderPainted(false);
		redPaint.setActionCommand("red");
		redPaint.addActionListener(this);
		leftPanel.add(redPaint);  

		// Green
		JButton greenPaint = new JButton();
		greenPaint.setBackground(Color.GREEN);
		greenPaint.setOpaque(true);
		greenPaint.setBorderPainted(false);
		greenPaint.setActionCommand("green");
		greenPaint.addActionListener(this);
		leftPanel.add(greenPaint);  

		// Blue
		JButton bluePaint = new JButton();
		bluePaint.setBackground(Color.BLUE);
		bluePaint.setOpaque(true);
		bluePaint.setBorderPainted(false);
		bluePaint.setActionCommand("blue");
		bluePaint.addActionListener(this);
		leftPanel.add(bluePaint);  

		
		holder.add(leftPanel, BorderLayout.WEST);

		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 3)); 

		// add circle select button
		JButton circleSelect = new JButton("Circle");
		circleSelect.setOpaque(true);
		circleSelect.setActionCommand("circle");
		circleSelect.addActionListener(this);
		topPanel.add(circleSelect); 
		// add line select button
		JButton lineSelect = new JButton("Line");
		lineSelect.setOpaque(true);
		lineSelect.setActionCommand("line");
		lineSelect.addActionListener(this);
		topPanel.add(lineSelect);  

		//add shape selectors to the main panel
		holder.add(topPanel, BorderLayout.NORTH);

		// add paint panel (the canvas where shapes are drawn)
		paintPanel = new PaintingPanel();
		paintPanel.addMouseListener(this);
		holder.add(paintPanel, BorderLayout.CENTER);

		//Chat Panel
		this.chat = new JPanel();
		this.chat.setLayout(new BorderLayout());
		this.chat.setBackground(Color.ORANGE);

		//input field
		JPanel inputField = new JPanel();
		inputField.setLayout(new BorderLayout());

		//add message input text field
		this.msgInput = new JTextArea();
		this.msgInput.setBackground(Color.GRAY);

		this.msgInput.setLineWrap(true);
		this.msgInput.setWrapStyleWord(true);
		this.msgInput.addMouseListener(this);
		this.msgInput.addFocusListener(this);
		this.msgInput.addKeyListener(this);
		//set initial pretext
		setMsgPretext();

		//add "Send Message" Button to input field
		JButton sendMsg = new JButton("Send Message");
		sendMsg.setActionCommand("sendMsg");
		sendMsg.addActionListener(this);

		inputField.add(this.msgInput,BorderLayout.CENTER);
		inputField.add(sendMsg, BorderLayout.EAST);

		chat.add(inputField, BorderLayout.NORTH);
		feed = new JTextArea();
		JScrollPane feedContainer = new JScrollPane(feed);

		feed.setBackground(Color.DARK_GRAY);
		//		feed.setAutoscrolls(true);
		feed.setLineWrap(true);
		feed.setWrapStyleWord(true);
		feed.setEditable(false);
		feed.setForeground(Color.WHITE);

		chat.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT/4));
		chat.add(feedContainer, BorderLayout.CENTER);

		//add chat to holder
		holder.add(chat, BorderLayout.SOUTH);

		// Lastly, connect the holder to the JFrame
		setContentPane(holder);

		// And make it visible to layout all the components on the screen
		setVisible(true);

		//listen for updates from the Hub
		Thread pullUpdates = new Thread() {
			public void run() {
				while(true) {
					try {
						synchronized(ois) {
							Object obj = ois.readObject();

							if(obj.getClass().toString().contains("String")) {
								String newChat = (String) obj;
								feed.append(newChat);
							} else {
								PaintingPrimitive newShape = (PaintingPrimitive) obj;
								paintPanel.addPrimitive(newShape);
								paintPanel.repaint();
							}
						}
					} catch (ClassNotFoundException | IOException e) {
						System.out.println("PAINTER ERROR: readObject");
						e.printStackTrace();
					}
				}
			}
		};
		pullUpdates.start();
	}

	//Contacting Hub
	public void pullUpdate(ObjectInputStream ois) {
		while(true) {
			try {
				Object update = ois.readObject();

				System.out.println(update.getClass().toString());

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public  void pushUpdate(ObjectOutputStream oos, Object obj) {
		while(true) {
			try {
				oos.writeObject(obj);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public synchronized void actionPerformed(ActionEvent e) {
		String select = e.getActionCommand();

		switch(select) {
		case "red":
			this.color = Color.RED;
			break;
		case "green":
			this.color = Color.GREEN;
			break;
		case "blue":
			this.color = Color.BLUE;
			break;
		case "circle":
			this.shape = "circle";
			break;
		case "line":
			this.shape = "line";
			break;
		case "sendMsg":
			//ensure pretext is not being sent, nor a blank message
			if(!this.msgInput.getForeground().equals(Color.LIGHT_GRAY) &&
					!this.msgInput.getText().equals("")) {
				//send input text to feed
				String message = this.getTitle() + ": " + this.msgInput.getText();
				try {
					oos.writeObject(message);
				} catch (IOException e1) {
					System.out.println("PAINTER ERROR: failed to send message");
					e1.printStackTrace();
				}
				//reset input
				setMsgPretext();
			}
			break;
		default:
			System.out.println("Error: invalid actionPerformed");
		}
	}

	@Override
	public synchronized void mousePressed(MouseEvent e) {
		//set Point on press
		this.startPoint=e.getPoint();
		if(this.paintPanel.contains(this.startPoint) && this.msgInput.isFocusOwner()) {
			this.paintPanel.grabFocus();
		}
	}


	@Override
	public synchronized void mouseReleased(MouseEvent e) {
		//save Point on release
		Point endPoint = e.getPoint();

		//test for shape type -> Draw shape accordingly 
		switch(shape) {
		case "circle":
			this.shapeToDraw = new Circle(this.startPoint, endPoint, color);
			break;
		case "line":
			this.shapeToDraw = new Line(this.startPoint, endPoint, color);
			break;
		default:
			System.out.println("Error mouseReleased");
			System.exit(0);
		}

		//send shape to Hub (via PainterThread)
		try {
			oos.writeObject(shapeToDraw);
		} catch (IOException e1) {
			System.out.println("PAINTER ERROR: write shape");
			e1.printStackTrace();
			System.exit(0);
		}
	}
	
	@Override
	public synchronized void mouseClicked(MouseEvent e) {
		if(this.msgInput.contains(e.getPoint())){
			//delete msgInput pretext on focus
			clearMsgPretext();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		//TODO: allow users to hit ENTER instead of submit button every time
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		clearMsgPretext();
	}
	
	//helper method to clear the msgInput pretext
	public void clearMsgPretext() {
		if(this.msgInput.getText().equals("Message")) {
			this.msgInput.setForeground(Color.BLACK);
			this.msgInput.setText("");
		}
	}

	//helper method to reset the msgInput pretext
	public void setMsgPretext() {
		this.msgInput.setForeground(Color.LIGHT_GRAY);
		this.msgInput.setText("Message");
	}
	
	public static void main(String[] args) {
		//prompt for painter's name
		String username = JOptionPane.showInputDialog("Enter your name");

		//if user hits cancel, don't make Painter
		if(null == username) {
			System.exit(0);
		}
		//make panel
		new Painter(username);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}
	@Override
	public void mouseMoved(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void focusLost(FocusEvent e) {}
}
