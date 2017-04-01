package helpers.spineVariation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

import helpers.base.MapHelper;
import models.spineVariation.SpineLandMap;

public class SpineTestPane extends JPanel {
	private static final long serialVersionUID = 1L;
	public static SpineLandMap spineLandMap;
	public String spineString;
	private int growtXY = 1;
	private int large = 35;
	private int width = 35;
	private boolean stringedType;
	
	public SpineTestPane(boolean stringedType, String stringify, int large, int width) {
		this.large = large;
		this.width = width;
		this.stringedType = stringedType;
		this.spineString = stringify;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(large, width);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(stringedType){
		   gramaticalSimulation(g);
		}else{
			normalSimulation(g); 
		}
	}

	private void normalSimulation(Graphics g) {
		int growthX = 0, growthY = 0;
		for (int x = 0; x < spineLandMap.getPointsx(); x++) {
			for (int y = spineLandMap.getPointsy() - 1; y >= 0; y--) {
				String type = spineLandMap.findPoint(MapHelper.formKey(x, y)).getType();
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
        String[] mapLines = spineString.split("\\.");
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
                        color = Color.RED;
                        break;
                    case "e":
                    case "n":
                        color = Color.MAGENTA;
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
				g.fillRect(growthX, growthY, (growthX+ growtXY +repetitions), (growthY + growtXY));
                growthX += repetitions;
            }
            growthX = 0;
            growthY += growtXY;
        }
	}
}