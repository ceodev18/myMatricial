package helpers.clusterVariation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import helpers.base.MapHelper;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandRoute;

public class ClusterTestPane extends JPanel {
	private static final long serialVersionUID = 1L;
	private ClusterLandMap clusterLandMap;
	public String clusterString;
	private int growtXY = 1;
	private int large = 35;
	private int width = 35;
	private boolean stringedType;
	private List<ClusterLandRoute> landRoutes;

	public ClusterTestPane(ClusterLandMap clusterLandMap, List<ClusterLandRoute> landRoutes, int large, int width) {
		this.stringedType = false;
		this.clusterLandMap = clusterLandMap;
		this.landRoutes = landRoutes;
		this.large = large;
		this.width = width;
	}
	
	public ClusterTestPane(boolean stringedType, String stringify, List<ClusterLandRoute> landRoutes, int large,
			int width) {
		this.large = large;
		this.width = width;
		this.stringedType = stringedType;
		this.landRoutes = landRoutes;
		this.clusterString = stringify;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(large, width);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (stringedType) {
			gramaticalSimulation(g);
		} else {
			normalSimulation(g);
		}
	}

	private void normalSimulation(Graphics g) {
		int growthX = 0, growthY = 0;
			for (int y = 0; y < clusterLandMap.getPointsy(); y++) {
				for (int x = 0; x < clusterLandMap.getPointsx() ; x++) {
				String type = clusterLandMap.findPoint(MapHelper.formKey(x, y)).getType();
				switch (type) {
				case "a":
				case "b":
				case "c":
				case "w":
				case "t":
					g.setColor(Color.GRAY);
					break;
				case "p":
					g.setColor(Color.GREEN);
					break;
				case "l":
				case ".":
					g.setColor(Color.RED);
					break;
				case "e":
				case "n":
					g.setColor(Color.MAGENTA);
					break;
				case "z":// contributions
					g.setColor(Color.YELLOW);
					break;
				case " ":
					g.setColor(Color.WHITE);
					break;
				default: // is an avenue or a street
					int houseLot = Integer.valueOf(type);
					switch (houseLot % 9) {
					case 1:
						g.setColor(Color.BLACK);
						break;
					case 2:
						g.setColor(Color.LIGHT_GRAY);
						break;
					case 3:
						g.setColor(Color.CYAN);
						break;
					default:
						g.setColor(Color.PINK);
						break;
					}

					break;
				}

				g.fillRect(growthX, growthY, (growthX + growtXY), (growthY + growtXY));
				growthX += growtXY;

			}
			growthX = 0;
			growthY += growtXY;
		}
		
		for (int i = 0; i < landRoutes.size(); i++) {
			int xy[] = ClusterMapHelper.breakKey(landRoutes.get(i).getInitialPointId());
			g.setColor(Color.BLACK);
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0], xy[1]);
			g.fillOval(xy[0], xy[1], 10, 10);
		}
	}

	private void gramaticalSimulation(Graphics g) {
		Color color;
		String[] mapLines = clusterString.split("\\.");
		int growthX = 0, growthY = 0;
		for (int x = 0; x < mapLines.length; x++) {
			String[] lineSymbol = mapLines[x].split("\\,");
			int growtXY = 1;
			for (int y = 0; y < lineSymbol.length; y++) {
				String type = String.valueOf(lineSymbol[y].charAt(0));
				switch (type) {/* 39 + 40*0 | 0+ 40*1 */
				case "a":
				case "b":
				case "c":
				case "w":
				case "t":
					color = Color.GRAY;
					break;
				case "p":
					color = Color.GREEN;
					break;
				case "l":
					color = Color.RED;
					break;
				case "e":
				case "n":
					color = Color.MAGENTA;
					break;
				case "z":
					color = Color.YELLOW;
					break;
				case " ":
					color = Color.WHITE;
					break;
				default: // is an avenue or a street
					int houseLot = Integer.valueOf(type);
					switch (houseLot % 4) {
					case 0:
						color = Color.BLUE;
						break;
					case 1:
						color = Color.BLACK;
						break;
					case 2:
						color = Color.MAGENTA;
						break;
					case 3:
						color = Color.WHITE;
						break;
					default:
						color = Color.getColor("#ff69b4");
						break;
					}
					break;
				}
				int repetitions = Integer.parseInt(lineSymbol[x].substring(1));
				lineSymbol[x] = null;
				g.setColor(color);
				g.fillRect(growthX, growthY, (growthX + growtXY + repetitions), (growthY + growtXY));
				growthX += repetitions;
			}
			growthX = 0;
			growthY += growtXY;
		}

		for (int i = 0; i < landRoutes.size(); i++) {
			int xy[] = ClusterMapHelper.breakKey(landRoutes.get(i).getInitialPointId());
			g.setColor(Color.BLACK);
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0], xy[1]);
			g.fillOval(xy[0], xy[1], 10, 10);
		}
	}
}