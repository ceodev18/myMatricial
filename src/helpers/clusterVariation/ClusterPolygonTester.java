package helpers.clusterVariation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import models.clusterVariation.ClusterPolygon;

public class ClusterPolygonTester extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<ClusterPolygon> clusterPolygons;

	public ClusterPolygonTester(List<ClusterPolygon> clusterPolygons) {
		this.clusterPolygons = clusterPolygons;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(2000, 2000);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int count = 0;
		
		for (int x = 0; x < clusterPolygons.size(); x++) {
			ClusterPolygon clusterPolygon = clusterPolygons.get(x);
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
			
			for (int i = 0; i < clusterPolygon.getPoints().size(); i++) {
				int[] up = ClusterMapHelper.breakKey(clusterPolygon.getPoints().get(i));
				int[] down = ClusterMapHelper
						.breakKey(clusterPolygon.getPoints().get((i + 1) % clusterPolygon.getPoints().size()));
				g.drawLine(up[0], up[1], down[0], down[1]);
			}
			
			count++;
		}
	}

}