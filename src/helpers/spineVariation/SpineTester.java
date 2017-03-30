package helpers.spineVariation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.spineVariation.SpineLotizationAlgorithm;
import algorithm.spineVariation.LSystemSpineAlgorithm;
import algorithm.spineVariation.LSystemSpineAlgorithm;
import helpers.base.MapHelper;
import helpers.clusterVariation.ClusterTester;
import helpers.spineVariation.SpineDirectionHelper;
import interfaces.spineVariation.SpineConfiguration;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandMap;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandMap;

public class SpineTester {
	public static void main(String[] argv){
		long startTime = System.nanoTime();
		//MAPA 1
		int large=1600+1,width=	700+1;
		SpineLandMap spineLandMap = new SpineLandMap(large, width);
		

		List<SpineLandPoint> polygon = new ArrayList<>();
		SpineLandPoint landPoint = new SpineLandPoint(1, 50);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(400, 600);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(1600, 600);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint( 1600, 50);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(1,50);
		polygon.add(landPoint);
		spineLandMap.createBorderFromPolygon(polygon);
		//entry point 353 31
		List<SpineLandPoint> entryPoints = new ArrayList<>();	
		//200 representa la distancia del borde del poligono al inicio
		//landPoint = new SpineLandPoint(200,571);
		landPoint = new SpineLandPoint(150,256);
		entryPoints.add(landPoint);
		
		// replace this LSYSTEM  by For loop
		LSystemSpineAlgorithm.landMap=spineLandMap;
		for (SpineLandPoint entryPoint : entryPoints) {
			int direction = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					spineLandMap.getCentroid());
			LSystemSpineAlgorithm.createRouteVariation(entryPoint.getId(), direction,
					SpineConfiguration.ARTERIAL_BRANCH);
			break;
		}
		LSystemSpineAlgorithm.landMap.printMapToFileNew();
		// 4. We clusterize the points
		LSystemSpineAlgorithm.clusterize();
		//LSystemSpineAlgorithm.landMap.printMapToFileNew();
		
		// 5. Zonification
		SpineLotizationAlgorithm.spineLandMap = LSystemSpineAlgorithm.landMap;
		SpineLotizationAlgorithm.zonify(150,256-1,large);
		//SpineLotizationAlgorithm.zonify(31+1,353-1); //punto de inicio
		

		//LSystemSpineAlgorithm.landMap.printMapToFile();
		SpineLotizationAlgorithm.spineLandMap.printMapToFileNew();
		//changeLbyE();
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");
		//ClusterLotizationAlgorithm.landMap.printMapToFile();
		//String compressedString = ClusterLotizationAlgorithm.landMap.stringify();
		//System.out.println("Compressed String lenght: " + compressedString.length());
		
		endTime = System.nanoTime();
		duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Response build finished in " + duration + "s");
		
		TestPane.spineLandMap = LSystemSpineAlgorithm.landMap;
		TestPane.large = large;
		TestPane.width = width;
		new SpineTester();
		
		
	}
	public SpineTester() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}

				JFrame frame = new JFrame("Testing");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				JScrollPane scrPane = new JScrollPane(new TestPane());
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
	public static class TestPane extends JPanel {
		private static final long serialVersionUID = 1L;
		public static SpineLandMap spineLandMap;
		private int growtXY = 1;
		public static int large = 35;
		public static int width = 35;

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(large, width);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int growthX = 0, growthY = 0;
			for (int y = spineLandMap.getPointsy() - 1; y >= 0; y--) {
				for (int x = 0; x < spineLandMap.getPointsx(); x++) {
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
						g.setColor(Color.RED);
						break;
					case "e":
						g.setColor(Color.BLUE);
						break;
					case ".":
					case "n":
						g.setColor(Color.MAGENTA);
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
						case 4:
							g.setColor(Color.WHITE);
							break;
						case 5:
							g.setColor(Color.YELLOW);
							break;
						case 6:
							g.setColor(Color.decode("#472544"));
							break;
						case 7:
							g.setColor(Color.ORANGE);
							break;
						case 8:
							g.setColor(Color.BLUE);
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
	}
}
