package helpers.matricial2035Variation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import models.matricial2035Variation.Matricial2035Polygon;

public class Matricial2035PolygonTester extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<Matricial2035Polygon> Matricial2035Polygons;

	public Matricial2035PolygonTester(List<Matricial2035Polygon> Matricial2035Polygons) {
		this.Matricial2035Polygons = Matricial2035Polygons;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(1000, 1300);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int count = 0;
		
		for (int x = 0; x < Matricial2035Polygons.size(); x++) {
			Matricial2035Polygon Matricial2035Polygon = Matricial2035Polygons.get(x);
			switch (count % 6) {
			case 0:
				g.setColor(Color.GREEN);
				break;
			case 1:
				g.setColor(Color.RED);
				break;
			case 2:
				g.setColor(Color.BLUE);
				break;
			case 3:
				g.setColor(Color.BLACK);
				break;
			case 4:
				g.setColor(Color.PINK);
				break;
			case 5:
				g.setColor(Color.MAGENTA);
				break;
			}
			
			int[] up = Matricial2035MapHelper.breakKey(Matricial2035Polygon.getPoints().get(0));
			int[] down = Matricial2035MapHelper
					.breakKey(Matricial2035Polygon.getPoints().get((1) % Matricial2035Polygon.getPoints().size()));
			
			for (int i = 0; i < Matricial2035Polygon.getPoints().size(); i++) {
				up = Matricial2035MapHelper.breakKey(Matricial2035Polygon.getPoints().get(i));
				down = Matricial2035MapHelper
						.breakKey(Matricial2035Polygon.getPoints().get((i + 1) % Matricial2035Polygon.getPoints().size()));
				g.drawLine(up[0], up[1], down[0], down[1]);
			}
			
			count++;
			g.drawChars((""+count).toCharArray(), 0, (""+count).length(), up[0], up[1]);
		}
	}

}