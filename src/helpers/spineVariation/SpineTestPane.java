package helpers.spineVariation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import helpers.clusterVariation.ClusterDirectionHelper;
import helpers.clusterVariation.ClusterMapHelper;
import models.clusterVariation.ClusterLandRoute;
import models.spineVariation.SpineLandMap;
import models.spineVariation.SpineLandRoute;

public class SpineTestPane extends JPanel {
	private static final long serialVersionUID = 1L;
	public static SpineLandMap spineLandMap;
	public String spineString;
	private int growtXY = 1;
	private int large = 35;
	private int width = 35;
	//private boolean stringedType;
	private int stringedType;
	private List<SpineLandRoute> landRoutes;
	/*
	public SpineTestPane(boolean stringedType, String stringify, int large, int width) {
		this.large = large;
		this.width = width;
		this.stringedType = stringedType;
		this.spineString = stringify;
	}
*/
	public SpineTestPane(int stringedType, String stringify, List<SpineLandRoute> landRoutes, int large,
			int width) {
		this.large = large;
		this.width = width;
		this.stringedType = stringedType;
		this.landRoutes = landRoutes;
		this.spineString = stringify;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(large, width);
	}

	/*@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(stringedType){
		   gramaticalSimulation(g);
		}else{
			normalSimulation(g); 
		}
	}*/
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
		String[] mapLines = spineString.split("\\,");
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

				/*String legend = buildingSymbols[5] + "x" + buildingSymbols[6];
				g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 5));
				g.drawChars(legend.toCharArray(), 0, legend.length(), coords[0], coords[1]);*/
				break;
			case "g":
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
			int xy[] = ClusterMapHelper.breakKey(landRoutes.get(i).getInitialPointId());
			g.setColor(Color.BLACK);
			g.drawChars(landRoutes.get(i).getType().toCharArray(), 0, landRoutes.get(i).getType().length(), xy[0],
					xy[1]);
			g.fillOval(xy[0], xy[1], 10, 10);
		}
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
		for (int x = 0; x < spineLandMap.getPointsx(); x++) {
			for (int y = spineLandMap.getPointsy() - 1; y >= 0; y--) {
				String type = spineLandMap.findPoint(SpineMapHelper.formKey(x, y)).getType();
				switch (type) {/* 39 + 40*0 | 0+ 40*1 */
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
	}

	private void gramaticalSimulation(Graphics g) {
        Color color;
        String[] mapLines = spineString.split("\\|");
        int growthX = 0, growthY = 0;
        for (int y = mapLines.length - 1; y >= 0; y--) {
            String [] lineSymbol = mapLines[y].split("\\,");
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
                    case "e":
                    case "n":
                        color = Color.MAGENTA;
                        break;
                    case ".":
                        color = Color.WHITE;
                    case " ":
                        color = Color.WHITE;
                        break;
                    case "z":
                        color = Color.YELLOW;
                        break;
                    default: // is an avenue or a street
                      //  System.out.println("With type=" + type + " Christian error " + lineSymbol[x]);
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
				g.fillRect(growthX, growthY, (growthX+ growtXY +repetitions), (growthY + growtXY));
                growthX += repetitions;
            }
            growthX = 0;
            growthY += growtXY;
        }
	}
	private int[] interpretNonOrhtogonalBuilding(String[] buildingSymbols) {
		int[] coords = new int[buildingSymbols.length - 1];
		for (int i = 1; i < buildingSymbols.length; i++) {
			coords[i - 1] = Integer.parseInt(buildingSymbols[i]);
		}
		return coords;
	}
}