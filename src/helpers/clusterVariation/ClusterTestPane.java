package helpers.clusterVariation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandRoute;

public class ClusterTestPane extends JPanel {
	private static final long serialVersionUID = 1L;
	private ClusterLandMap clusterLandMap;
	public String clusterString;
	private int growtXY = 1;
	private int large = 35;
	private int width = 35;
	private int stringedType;
	private List<ClusterLandRoute> landRoutes;
	private List<Integer> landmapNodes;

	public ClusterTestPane(ClusterLandMap clusterLandMap, List<ClusterLandRoute> landRoutes, int large, int width) {
		this.stringedType = 0;
		this.clusterLandMap = clusterLandMap;
		this.landRoutes = landRoutes;
		this.large = large;
		this.width = width;
	}

	public ClusterTestPane(int stringedType, List<Integer> landmapNodes, String stringify,
			List<ClusterLandRoute> landRoutes, int large, int width) {
		this.large = large;
		this.width = width;
		this.stringedType = stringedType;
		this.landRoutes = landRoutes;
		this.clusterString = stringify;
		this.landmapNodes = landmapNodes;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(large, width);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		switch (stringedType) {
		case 0:
			normalSimulation(g);
			break;
		case 1:
			gramaticalSimulation(g);
			break;
		case 2:
			linearGrammarSimulation(g);
			break;
		}
	}

	private void linearGrammarSimulation(Graphics g) {
		String[] mapLines = clusterString.split("\\,");

		for (int i = 0; i < landmapNodes.size(); i++) {
			int[] initialXY = ClusterMapHelper.breakKey(landmapNodes.get(i));
			int[] finalXY = ClusterMapHelper.breakKey(landmapNodes.get((i + 1) % landmapNodes.size()));
			g.drawLine(initialXY[0], initialXY[1], finalXY[0], finalXY[1]);
		}

		int house = 0;
		for (int y = 0; y < mapLines.length; y++) {
			String[] buildingSymbols = mapLines[y].split("\\-");
			switch (buildingSymbols[0]) {
			case "l":
				int[] coords = interpretBuilding(buildingSymbols[1], buildingSymbols[2], buildingSymbols[3],
						buildingSymbols[4], buildingSymbols[5], buildingSymbols[6]);
				g.drawLine(coords[0], coords[1], coords[2], coords[3]);
				g.drawLine(coords[2], coords[3], coords[4], coords[5]);
				g.drawLine(coords[4], coords[5], coords[6], coords[7]);
				g.drawLine(coords[6], coords[7], coords[0], coords[1]);

				/*
				 * String legend = buildingSymbols[5] + "x" +
				 * buildingSymbols[6]; g.setFont(new Font(Font.SANS_SERIF,
				 * Font.PLAIN, 5)); g.drawChars(legend.toCharArray(), 0,
				 * legend.length(), coords[0], coords[1]);
				 */
				break;
			case "n":
				coords = interpretNonOrhtogonalBuilding(buildingSymbols);
				g.drawLine(coords[0], coords[1], coords[2], coords[3]);
				g.drawLine(coords[2], coords[3], coords[4], coords[5]);
				g.drawLine(coords[4], coords[5], coords[6], coords[7]);
				g.drawLine(coords[6], coords[7], coords[0], coords[1]);
				System.out.println("Coordinates(" + house + "): " + coords[0] + "," + coords[1] + "->" + coords[2] + ","
						+ coords[3] + "->" + coords[4] + "," + coords[5] + "->" + coords[6] + "," + coords[7]);
				g.drawChars(("" + house).toCharArray(), 0, ("" + house).length(), coords[0], coords[1]);
				house++;
				/*
				 * String legend = buildingSymbols[5] + "x" +
				 * buildingSymbols[6]; g.setFont(new Font(Font.SANS_SERIF,
				 * Font.PLAIN, 5)); g.drawChars(legend.toCharArray(), 0,
				 * legend.length(), coords[0], coords[1]);
				 */
				break;
			case "g":
				coords = interpretNonOrhtogonalBuilding(buildingSymbols);
				for (int i = 0; i < coords.length; i+=2) {
					g.drawLine(coords[i], coords[i + 1], coords[(i + 2) % coords.length],
							coords[(i + 3) % coords.length]);
				}
				break;
			}
		}

		for (int i = 0; i < landRoutes.size(); i++) {
			int xy[] = ClusterMapHelper.breakKey(landRoutes.get(i).getInitialPointId());
			g.setColor(Color.BLACK);
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0]-(landRoutes.get(i).getType().equals("b")?0:10),
					xy[1]);
			g.fillOval(xy[0]-(landRoutes.get(i).getType().equals("b")?0:10), xy[1], 10, 10);
		}
	}

	private int[] interpretNonOrhtogonalBuilding(String[] buildingSymbols) {
		int[] coords = new int[buildingSymbols.length - 1];
		for (int i = 1; i < buildingSymbols.length; i++) {
			coords[i - 1] = Integer.parseInt(buildingSymbols[i]);
		}
		return coords;
	}

	private int[] interpretBuilding(String initialX, String finalX, String driveDirection, String growDirection,
			String sideSize, String depthSize) {
		int[] coords = new int[8];
		coords[0] = Integer.parseInt(initialX);
		coords[1] = Integer.parseInt(finalX);

		int[] xy = ClusterMapHelper.moveKeyByOffsetAndDirection(coords, Integer.parseInt(sideSize),
				Integer.parseInt(driveDirection));
		coords[2] = xy[0];
		coords[3] = xy[1];

		xy = ClusterMapHelper.moveKeyByOffsetAndDirection(xy, Integer.parseInt(depthSize),
				Integer.parseInt(growDirection));
		coords[4] = xy[0];
		coords[5] = xy[1];

		xy = ClusterMapHelper.moveKeyByOffsetAndDirection(xy, Integer.parseInt(sideSize),
				ClusterDirectionHelper.oppositeDirection(Integer.parseInt(driveDirection)));
		coords[6] = xy[0];
		coords[7] = xy[1];
		return coords;
	}

	private void normalSimulation(Graphics g) {
		int growthX = 0, growthY = 0;
		for (int y = 0; y < clusterLandMap.getPointsy(); y++) {
			for (int x = 0; x < clusterLandMap.getPointsx(); x++) {
				String type = clusterLandMap.findPoint(ClusterMapHelper.formKey(x, y)).getType();
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
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0],
					xy[1]);
			g.fillOval(xy[0], xy[1], 10, 10);
		}
	}

	private void gramaticalSimulation(Graphics g) {
		Color color;
		String[] mapLines = clusterString.split("\\.");
		int growthX = 0, growthY = 0;
		for (int y = 0; y < mapLines.length; y++) {
			String[] lineSymbol = mapLines[y].split("\\,");
			int growtXY = 1;
			for (int x = 0; x < lineSymbol.length; x++) {
				String type = String.valueOf(lineSymbol[x].charAt(0));
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
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0],
					xy[1]);
			g.fillOval(xy[0], xy[1], 10, 10);
		}
	}

}