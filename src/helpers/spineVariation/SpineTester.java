package helpers.spineVariation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import algorithm.spineVariation.SpineAlgorithm;
import helpers.spineVariation.SpineDirectionHelper;
import interfaces.spineVariation.SpineConfiguration;
import models.spineVariation.SpineLandPoint;
import models.view.AlgorithmView;
import models.configuration.ConfigurationMatrix;
import models.spineVariation.SpineLandMap;

public class SpineTester {
	public static void main(String[] argv){
		long startTime = System.nanoTime();
		//MAPA 1
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");
		
		AlgorithmView algorithmView = new AlgorithmView("spine-error.txt");
		ConfigurationMatrix configurationMatrix = new ConfigurationMatrix(algorithmView);
		//int large=969 +1,width=899 +1;
		int large = algorithmView.getxSize(), width = algorithmView.getySize();
		
		SpineLandMap spineLandMap = new SpineLandMap(large, width);
		List<SpineLandPoint> polygon = new ArrayList<>();
		List<Integer> intVertex = algorithmView.getCartVertexgeocoords();
		List<Integer> listData= new ArrayList<>();
		for (int i = 0; i < algorithmView.getVertexgeocoords().size(); i += 2) {
			System.out.println("intVertex.get(i)");
			System.out.println(intVertex.get(i));
			System.out.println("intVertex.get(i+1)");
			System.out.println(intVertex.get(i+1));
			listData.add(intVertex.get(i));
			listData.add(intVertex.get(i+1));
			SpineLandPoint landPoint = new SpineLandPoint(intVertex.get(i), intVertex.get(i + 1));
			polygon.add(landPoint);
		}
		SpineLandPoint landPoint = new SpineLandPoint(intVertex.get(0), intVertex.get(1));
		polygon.add(landPoint);
		
		
		spineLandMap.createBorderFromPolygon(polygon);
		
		List<SpineLandPoint> entryPoints = new ArrayList<>();
		landPoint = new SpineLandPoint(algorithmView.getCartEntrygeocoords().get(0),
				algorithmView.getCartEntrygeocoords().get(1));
		entryPoints.add(landPoint);
		
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");
		
		// replace this LSYSTEM  by For loop
		
		// TRUE BEGINNING OF THE ALGORITHM
		
		//SpineLandMap landMap = new SpineLandMap(large, width);
		// 2. we create the border from the polygon
		spineLandMap.createMapBorder(polygon);
		//System.out.println("configurationMatrix.getConfiguration().get(0)");
		//System.out.println(configurationMatrix.getConfiguration().get(0));
		spineLandMap.setConfiguration(configurationMatrix.getConfiguration().get(0));
		
		// 3. We create the entry points for the main routes
		SpineAlgorithm spineAlgorithm = new SpineAlgorithm();
		spineAlgorithm.setLandMap(spineLandMap);
		spineAlgorithm.setWidth(width);
		spineAlgorithm.setEntryX(algorithmView.getCartEntrygeocoords().get(0));
		spineAlgorithm.setEntryY(algorithmView.getCartEntrygeocoords().get(1));
		

		spineAlgorithm.setLarge(large);
		//we painting branch
		for (SpineLandPoint entryPoint : entryPoints) {
			int direction =SpineDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					spineLandMap.getCentroid());
			spineAlgorithm.createRouteVariation(entryPoint.getId(), direction,SpineConfiguration.ARTERIAL_BRANCH);
		//	break;
		}
		
		// 4. We clusterize the points
		int directionBranch = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(new SpineLandPoint(algorithmView.getCartEntrygeocoords().get(0), algorithmView.getCartEntrygeocoords().get(1)),
				spineLandMap.getCentroid());
		spineAlgorithm.spineizeV2(directionBranch);
		// We make border
		spineAlgorithm.makeBorder(listData);
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after completed routes" + usedMemoryBefore / 1000000 + " in MB");
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore / 1000000 + " in MB");
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		
		endTime = System.nanoTime();
		duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Response build finished in " + duration + "s");
		
		System.out.println("Start canvas");
		SpineTestPane spineTestPane1 = new SpineTestPane(1, spineAlgorithm.getLandMap().stringify(), spineAlgorithm.getLandMap().getLandRoutes(), large, width);
		new SpineTester(spineTestPane1); 
		SpineTestPane spineTestPane2 = new SpineTestPane(2, spineAlgorithm.getLandMap().getNodes(),spineAlgorithm.getLandMap().getGrammar(), spineAlgorithm.getLandMap().getLandRoutes(), large, width);
		new SpineTester(spineTestPane2);
	}
	public SpineTester(SpineTestPane spineTestPane) {
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
				JScrollPane scrPane = new JScrollPane(spineTestPane);
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
}
