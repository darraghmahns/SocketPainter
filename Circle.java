package edu.du.cs.mahnsmcgee.painter;

import java.awt.*;

public class Circle extends PaintingPrimitive {
	protected Color colours;
	protected Point topLeft;
	protected Point bottomRight;

	public Circle(Point topLeft, Point bottomRight, Color colors) {
		super(colors);
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;

	}

	public void drawGeometry(Graphics g) {
		int radius = (int) ((Math.abs(topLeft.distance(bottomRight))));


		if ((topLeft.y > bottomRight.y) && (topLeft.x < bottomRight.x)) {
			g.drawOval(topLeft.x, bottomRight.y, radius, radius);
		} else if ((topLeft.y < bottomRight.y) && (topLeft.x >bottomRight.x)) {
			g.drawOval(bottomRight.x, topLeft.y, radius, radius);
		} else if (topLeft.y < bottomRight.y) {
			g.drawOval(topLeft.x, topLeft.y, radius, radius);
		} else if (topLeft.y > bottomRight.y) {
			g.drawOval(bottomRight.x, bottomRight.y, radius, radius);
		}
	}
}
