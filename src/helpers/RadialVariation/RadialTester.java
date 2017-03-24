package helpers.RadialVariation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.clusterVariation.ClusterLotizationAlgorithm;
import algorithm.clusterVariation.LSystemClusterAlgorithm;
import algorithm.radialVariation.LSystemRadialAlgorithm;
import helpers.base.MapHelper;
import helpers.clusterVariation.ClusterTester;
import helpers.RadialVariation.RadialDirectionHelper;
import interfaces.radialVariation.RadialConfiguration;
import models.RadialVariation.RadialLandPoint;
import helpers.RadialVariation.RadialTester.TestPane;
import models.RadialVariation.RadialLandMap;

public class RadialTester {
	public static void main(String[] argv){
		long startTime = System.nanoTime();
		int large=900,width=900;
		RadialLandMap radialLandMap = new RadialLandMap(large, width);
		
		List<RadialLandPoint> polygon = new ArrayList<>();
		RadialLandPoint landPoint = new RadialLandPoint(0, 700);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(300, 900);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(800, 900);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(900, 100);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(100, 0);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(0, 700);
		polygon.add(landPoint);
		
	 
		radialLandMap.createBorderFromPolygon(polygon);
		
            int areaCentx=(300+800+900+100+0)/5;
			int areaCenty=(900+900+100+0+700)/5;
								
		radialLandMap.printMapToFileCentroid(areaCentx,areaCenty);
		
		public void TotalArea {
			//This process will be calculated by determinants
			int det1,det2;
			double tot;

			det1= (300*900)+(800*100)+(900*0)+(100*700)+(0*900);
			det2= (900*800)+(900*900) +(100*100) +(0*0) +(700*300);

			tot= 0.5*(det1-det2);
		};
		
		public void CentralPark(int tot){
			//Parks in the polygon are the 30% of the total (polygon's area)
		double parks,centralPark;
		
		parks = 0.30*tot;
		
		    //Finally, the area's central park is the 8% of the parks in general (It's always inside of te polygon)
		centralPark = 0.08*parks;
		}
		
		//entry point
		List<RadialLandPoint> entryPoints = new ArrayList<>();
		landPoint = new RadialLandPoint(0, 400);
		entryPoints.add(landPoint);
		
		// replace this LSYSTEM  by For loop
		LSystemRadialAlgorithm.landMap=radialLandMap;
		
		for (RadialLandPoint entryPoint : entryPoints) {
			int direction = RadialDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					radialLandMap.getCentroid());
			LSystemRadialAlgorithm.createRouteVariation(entryPoint.getId(), direction,
					RadialConfiguration.ARTERIAL_BRANCH);
			break;
		}
		
		// 4. We clusterize the points
		LSystemRadialAlgorithm.Radialize();
		
		/*
		 * // 5. Zonification
		ClusterLotizationAlgorithm.landMap = LSystemClusterAlgorithm.landMap;
		ClusterLotizationAlgorithm.zonify();
		
*/
		LSystemRadialAlgorithm.landMap.printMapToFile( );
		changeLbyE();
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");
		//ClusterLotizationAlgorithm.landMap.printMapToFile();
		//String compressedString = ClusterLotizationAlgorithm.landMap.stringify();
		//System.out.println("Compressed String lenght: " + compressedString.length());
		
		endTime = System.nanoTime();
		duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Response build finished in " + duration + "s");
		
		TestPane.RadialLandMap = LSystemRadialAlgorithm.landMap;
		TestPane.large = large;
		TestPane.width = width;
		new RadialTester(); 
		
				}
				
	public RadialTester() {
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
		public static RadialLandMap RadialLandMap;
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
			for (int y = RadialLandMap.getPointsy() - 1; y >= 0; y--) {
				for (int x = 0; x < RadialLandMap.getPointsx(); x++) {
					String type = RadialLandMap.findPoint(MapHelper.formKey(x, y)).getType();
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
	private static void changeLbyE(){
		 
		
	}
}
