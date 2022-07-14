package edu.du.cs.mahnsmcgee.painter;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

public class PaintingPanel extends JPanel {

	protected ArrayList<PaintingPrimitive> ppal;

	public PaintingPanel() {

		setBackground(Color.WHITE);

		ppal = new ArrayList<PaintingPrimitive>(10);

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (PaintingPrimitive obj : ppal) {
			obj.draw(g);
		}
	}

	public void addPrimitive(PaintingPrimitive obj) {
		this.ppal.add(obj);
	}

}