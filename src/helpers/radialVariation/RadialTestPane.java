package helpers.radialVariation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import helpers.radialVariation.RadialMapHelper;
import models.radialVariation.RadialLandMap;
import models.radialVariation.RadialLandRoute;

public class RadialTestPane extends JPanel {
	private static final long serialVersionUID = 1L;
	private RadialLandMap radialLandMap;
	public String radialString;
	private int growtXY = 1;
	private int large = 35;
	private int width = 35;
	private int stringedType;
	private List<RadialLandRoute> landRoutes;
	private List<Integer> landmapNodes;

	public RadialTestPane(RadialLandMap radialLandMap, List<RadialLandRoute> landRoutes, int large, int width) {
		this.stringedType = 0;
		this.radialLandMap = radialLandMap;
		this.landRoutes = landRoutes;
		this.large = large;
		this.width = width;
	}

	public RadialTestPane(int stringedType, List<Integer> landmapNodes, String stringify,
			List<RadialLandRoute> landRoutes, int large, int width) {
		this.large = large;
		this.width = width;
		this.stringedType = stringedType;
		this.landRoutes = landRoutes;
		this.radialString = stringify;
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
		String[] mapLines = radialString.split("\\,");
		for (int i = 0; i < landmapNodes.size(); i++) {
			int[] initialXY = RadialMapHelper.breakKey(landmapNodes.get(i));
			int[] finalXY = RadialMapHelper.breakKey(landmapNodes.get((i + 1) % landmapNodes.size()));
			g.drawLine(initialXY[0], initialXY[1], finalXY[0], finalXY[1]);
		}

		int house = 0;
		for (int y = 0; y < mapLines.length; y++) {
			String[] buildingSymbols = mapLines[y].split("\\-");
			switch (buildingSymbols[0]) {

			case "l":
				System.out.print("("+house+")");
				int [] coords = interpretNonOrhtogonalBuilding(buildingSymbols);
				for (int i = 0; i < coords.length; i+=2) {
					g.drawLine(coords[i], coords[i + 1], coords[(i + 2) % coords.length],
							coords[(i + 3) % coords.length]);
					System.out.print(coords[i] + ","+ coords[i + 1] +"->");
				}
				System.out.println();
				
				//g.drawChars(("" + house).toCharArray(), 0, ("" + house).length(), coords[0], coords[1]);
				house++;
				System.out.println();
				/* String legend = buildingSymbols[5] + "x" +
				 * buildingSymbols[6]; g.setFont(new Font(Font.SANS_SERIF,
				 * Font.PLAIN, 5)); g.drawChars(legend.toCharArray(), 0,
				 * legend.length(), coords[0], coords[1]);  */
				break;
			case "g":
				coords = interpretNonOrhtogonalBuilding(buildingSymbols);
				for (int i = 0; i < coords.length; i+=2) {
					g.drawLine(coords[i], coords[i + 1], coords[(i + 2) % coords.length],
							coords[(i + 3) % coords.length]);
				}
				//g.drawChars(("" + coords[0]).toCharArray(), 0, ("" + coords[0]).length(), coords[0], coords[1]);
				break;
			case "z":
				coords = interpretNonOrhtogonalBuilding(buildingSymbols);
				for (int i = 0; i < coords.length; i+=2) {
					g.drawLine(coords[i], coords[i + 1], coords[(i + 2) % coords.length],
							coords[(i + 3) % coords.length]);
				}
				//g.drawChars(("" + coords[0]).toCharArray(), 0, ("" + coords[0]).length(), coords[0], coords[1]);
				break;
			case "o":
				coords = interpretNonOrhtogonalBuilding(buildingSymbols);
				for (int i = 0; i < coords.length; i+=2) {
					g.drawLine(coords[i], coords[i + 1], coords[(i + 2) % coords.length],
							coords[(i + 3) % coords.length]);
				}
				//g.drawChars(("" + coords[0]).toCharArray(), 0, ("" + coords[0]).length(), coords[0], coords[1]);
				break;
			}
		}

		for (int i = 0; i < landRoutes.size(); i++) {
			int xy[] = RadialMapHelper.breakKey(landRoutes.get(i).getInitialPointId());
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

	private void normalSimulation(Graphics g) {
		int growthX = 0, growthY = 0;
		for (int y = 0; y < radialLandMap.getPointsy(); y++) {
			for (int x = 0; x < radialLandMap.getPointsx(); x++) {
				String type = radialLandMap.findPoint(RadialMapHelper.formKey(x, y)).getType();
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
			int xy[] = RadialMapHelper.breakKey(landRoutes.get(i).getInitialPointId());
			g.setColor(Color.BLACK);
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0],
					xy[1]);
			g.fillOval(xy[0], xy[1], 10, 10);
		}
	}

	private void gramaticalSimulation(Graphics g) {
		Color color;
		String[] mapLines = radialString.split("\\.");
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
			int xy[] = RadialMapHelper.breakKey(landRoutes.get(i).getInitialPointId());
			g.setColor(Color.BLACK);
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0],
					xy[1]);
			g.fillOval(xy[0], xy[1], 10, 10);
		}
	}
}