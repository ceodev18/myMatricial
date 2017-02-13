package algorithm;

import java.util.Random;
import interfaces.Constants;
import models.LandMap;
import models.LandPoint;
import models.LandRoute;

public class GreedyRouteAlgorithm {

	public static LandMap landMap;
	
	/*
	 * The 2 main route points should be random that way we generate the
	 * possibility of multiple maps
	 */
	/*
	 * 1. Create main route 2. Given the expectedHouseSize (for know in points)
	 * Every (Random 5-12) * expectedHouseSize make a sub route that branches
	 * from the main polygon 3. Sub route should end on the border of the
	 * polygon or the map 4. For each sub route (2) * expectedHouseSize make
	 * another parallel or semi-parallel branch to the main route. 5. The stop
	 * condition should be
	 */

	// MapHelper.delimiterPolygon(landMap, landPolygon);
	// I need the routes separated from the map. The map must be like a
	// greed that allows me
	// List<LandRoute> routes = new ArrayList<>();
	// recursive call changes, direction, SUB -> Main and bis)

	// we save the landMap variable and save the routes as Integer list
	public static void createRoute(int entryPointId, int direction, int type, int maxDeep,
			int currentDeep) {
		
		for(int w= currentDeep ; w< maxDeep; w++){
			// Route creation
			LandPoint currentPoint = landMap.findPoint(entryPointId, direction, type);
			if (currentPoint == null)
				return;// TODO means is already taken, it should verify wether by an
						// avenue or a subroute. For now nothing
			landMap.pointIsOfRoute(entryPointId, true);
			LandRoute landRoute = new LandRoute();
			landRoute.setType(type); // This will be the indicator on the print
										// section
			landRoute.getPoints().add(currentPoint.getId());

			while (!currentPoint.isMapLimit(direction)) {
				int nextPointId = currentPoint.findNeighbour(direction);

				if (nextPointId != -1) {// means it is already occupied
					currentPoint = landMap.findPoint(nextPointId, direction, type);
					landMap.pointIsOfRoute(nextPointId, true);
					if (currentPoint != null) {
						landRoute.getPoints().add(currentPoint.getId());
					} else {
						// routes.add(landRoute);
						return;
					}
				} else {
					// routes.add(landRoute);
					return;
				}
			}

			// routes.add(landRoute);
			// Route partition
			if (type == Constants.AVENUE_BRANCH) {
				// Random from 5 to 12
				Random random = new Random();
				int randomNumber = random.nextInt(12);
				int number = randomNumber < 5 ? randomNumber + 5 : randomNumber;

				currentDeep++;
				for (int i = number - 1; i < landRoute.getPoints().size(); i += number) {
					int pointId = landRoute.getPoints().get(i);

					if (direction == Constants.NORTH || direction == Constants.SOUTH) {
						createRoute(pointId, Constants.EAST, Constants.STREET_BRANCH, maxDeep, currentDeep);
						createRoute(pointId, Constants.WEST, Constants.STREET_BRANCH, maxDeep, currentDeep);
					} else {
						createRoute(pointId, Constants.NORTH, Constants.STREET_BRANCH, maxDeep, currentDeep);
						createRoute(pointId, Constants.SOUTH, Constants.STREET_BRANCH, maxDeep, currentDeep);
					}
				}
			} else {
				Random random = new Random();
				int randomNumber = random.nextInt(3); // stop (0) - alley (1) - new
														// avenue (2)
				switch (randomNumber) {
				case 0:
					return;
				case 1:
					for (int i = 2; i < landRoute.getPoints().size(); i += 3) {
						int pointId = landRoute.getPoints().get(i);
						if (direction == Constants.NORTH || direction == Constants.SOUTH) {
							createRoute(pointId, Constants.EAST, Constants.ALLEY_BRANCH, maxDeep, currentDeep);
							createRoute(pointId, Constants.WEST, Constants.ALLEY_BRANCH, maxDeep, currentDeep);
						} else {
							createRoute(pointId, Constants.NORTH, Constants.ALLEY_BRANCH, maxDeep, currentDeep);
							createRoute(pointId, Constants.SOUTH, Constants.ALLEY_BRANCH, maxDeep, currentDeep);
						}
					}
					break;
				case 2:
					for (int i = 2; i < landRoute.getPoints().size(); i += 3) {
						int pointId = landRoute.getPoints().get(i);
						if (direction == Constants.NORTH || direction == Constants.SOUTH) {
							createRoute(pointId, Constants.EAST, Constants.AVENUE_BRANCH, maxDeep, currentDeep);
							createRoute(pointId, Constants.WEST, Constants.AVENUE_BRANCH, maxDeep, currentDeep);
						} else {
							createRoute(pointId, Constants.NORTH, Constants.AVENUE_BRANCH, maxDeep, currentDeep);
							createRoute(pointId, Constants.SOUTH, Constants.AVENUE_BRANCH, maxDeep, currentDeep);
						}
					}
					break;
				}

			}	
		}
	}
} 