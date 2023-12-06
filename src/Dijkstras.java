import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dijkstras {
	private static final int NO_PARENT = -1;
	private static final int THRESHOLD = 250; // Adjust the threshold as needed
	private static String[] cityList = {"MillCity-NV","Eureka-NV","Wells-NV","Howe-ID","Jackpot-NV","Montello-NV","Owyhee-NV","Eugene-OR","Gresham-OR","Hillsbboro-OR","Bend-OR","Beaverton-OR","Medford-OR","Johnday-OR","Roseburg-OR","Bakercity-OR","Salem-OR","Newport-OR","Ashland-OR","Riley-OR","Seattle-WA","Spokane-WA","Tacoma-WA","Vancouver-WA","Bellevue-WA","Kent-WA","Leavenworth-WA","Yakima-WA","Richland-WA","Pullman-WA","WestWendover-NV","LosAngeles-CA","SanDiego-CA","SanJose-CA","SanFrancisco-CA","Fresno-CA","Sacramento-CA","LongBeach-CA","Redding-CA","Bakersfield-CA","Bishop-CA","Irvine-CA","Darwin-CA","Bayswater-CA","Arvin-CA","Albuquerque-NM","LasCruces-NM","RioRancho-NM","SantaFe-NM","Roswell-NM","SaltLakeCity-UT","WestValleyCity-UT","Provo-UT","WestJordan-UT","Sandy-UT","Orem-UT","Ogden-UT","Missoula-MT","GreatFalls-MT","Bozeman-MT","Butte-SilverBow-MT","Helena-MT","Kalispell-MT","Almosa-CO","cortez-CO","GrandJunction-CO","Pueblo-CO","Lamar-CO","Fortcollins-CO","Aspen-CO","Telluride-CO","Montrose-CO","Prescott-AZ","Buckeye-AZ","Sedona-AZ","Tucson-AZ","Phoenix-AZ","Chinle-AZ","Kingman-AZ","Laramie-WY","Gillette-WY","RockSprings-WY","Sheridan-WY","GreenRiver-WY","Evanston-WY","Lander-WY","Rawlins-WY","Casper-WY","Cody-WY","Dubois-WY","JordanValley-OR","Rome-OR","Adrian-OR","Nyssa-OR","Owyhee-OR","Williston-ND","Dickinson-ND","Bowman-NE","Harrison-NE","Imperial-NE","Alliance-NE","Grant-NS","ELPaso-TX","Tribune-NV","SharonSprings-KS","Loeti-KS","Boise-ID","Meridian-ID","Nampa-ID","IdahoFalls-ID","Pocatello-ID","Caldwell-ID","Ketchum-ID","Chilly-ID","May-ID","Donnelly-ID","Buhl-ID","Kamaih-ID","Butte-MT"};
	private static List<String> stringList = new ArrayList<>(Arrays.asList(cityList));
	static display_temperatures temperature;
	private float[][] adj_matrix = null;
	public static float mileage;

    public static float shortestDistance;
	static StringBuilder shortest_path = new StringBuilder();
    static StringBuilder path = new StringBuilder();
	public static int i;
	public static String endcity;

    public static float[] getDistances(String city1,String city2, float[][] adjMat){
        float[] data = new float[2];
        int i1 = findIndex(city1.split("-")[0]);
        int i2 = findIndex(city2.split("-")[0]);
        data[0] = adjMat[i1][i2];
        data[1] = (adjMat[i1][i2]/55);
        return data;

    }

    public static void display(String data, float[][] adjMat){
        String[] cities = data.split(",");

        for(int i=0;i<cities.length-1;i++){
            float[] distance = getDistances(cities[i],cities[i+1],adjMat);
            System.out.print(cities[i]);
            if(i!=cities.length-1){
                System.out.print("-----["+distance[0]+" Miles, Time = "+distance[1]+ " hrs]----->");
            }
        }
        System.out.println(cities[cities.length-1]);
    }
	
	public Dijkstras(String startcity, String endcity, display_temperatures temperature, float[][] adjacencymatrix, float mileage, boolean display) {
		int startVertex = 0; 
		int endVertex = 0; 
		this.temperature = temperature;
		this.mileage = mileage;
		this.adj_matrix = adjacencymatrix;
		this.endcity = endcity;
		
		// Iterate through the array and apply toLowerCase() to each element
        for (int i = 0; i < cityList.length; i++) {
        	cityList[i] = cityList[i].toLowerCase().replace (" ","");
//        	System.out.println(cityList[i]);
        }

		for (int i = 0; i < cityList.length; i++) {
			
            if (cityList[i].equals(startcity)) {
            	startVertex = i;
             //   break; // Exit the loop once the city is found
            }
            if(cityList[i].equals(endcity)){
            	endVertex = i;
            }
            if(startVertex != 0 && endVertex!= 0) {
            	break;
            } 
        }
		
		//System.out.println("startcity: " + startcity + " startVertex : " + startVertex + " endcity: " + endcity + " endVertex : " + endVertex);
		dijkstra(adj_matrix, startVertex, endVertex,display);
	}

    public static float getDistance(float[][] distanceData,String path){
        String[] cities = path.split(",");
        float distance = 0;
        for(int i=0;i<cities.length-1;i++){
            distance = distance + distanceData[findIndex(cities[i])][findIndex(cities[i+1])];
        }
        return distance;
    }
    private static int findIndex(String city) {
        for (int i = 0; i < cityList.length; i++) {
            if (cityList[i].toLowerCase().replaceAll("\\s", "").contains(city.toLowerCase().replaceAll("\\s", ""))) {
                return i;
            }
        }
        return 0;
    }
	
	
	private static void dijkstra(float[][] adjacencyMatrix, int startVertex, int destinationVertex,boolean display) {
        int nVertices = adjacencyMatrix[0].length;
        float[] shortestDistances = new float[nVertices];
        boolean[] added = new boolean[nVertices];

        for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
            shortestDistances[vertexIndex] = Integer.MAX_VALUE;
            added[vertexIndex] = false;
        }

        shortestDistances[startVertex] = 0;
        int[] parents = new int[nVertices];
        parents[startVertex] = NO_PARENT;

        for (int i = 1; i < nVertices; i++) {
            int nearestVertex = -1;
            float shortestDistance = Integer.MAX_VALUE;

            // Find the nearest vertex among the non-added vertices
            for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
                if (!added[vertexIndex] && shortestDistances[vertexIndex] < shortestDistance) {
                    nearestVertex = vertexIndex;
                    shortestDistance = shortestDistances[vertexIndex];
                }
            }

            if (nearestVertex == -1) {
                // No valid nearest vertex found, break the loop
                break;
            }

            added[nearestVertex] = true;

            for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
                float edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex];

                if (vertexIndex != nearestVertex && !added[vertexIndex] && edgeDistance > 0
                        && edgeDistance < THRESHOLD && (shortestDistance + edgeDistance) < shortestDistances[vertexIndex]) {
                    parents[vertexIndex] = nearestVertex;
                    shortestDistances[vertexIndex] = shortestDistance + edgeDistance;
                }
            }
        }

        printSolution(startVertex, destinationVertex, shortestDistances, parents, stringList, display);
    }

    private static void printSolution(int startVertex, int destinationVertex, float[] distances, int[] parents, List<String> stringList, boolean display) {
        int nVertices = distances.length;


        if (distances[destinationVertex] == Integer.MAX_VALUE) {
            System.out.println("No path found from " + startVertex + " to " + destinationVertex);
        } else {
            shortestDistance = distances[destinationVertex];
            if(display){
                System.out.println("\nDistance from " + stringList.get(startVertex) + " to " + stringList.get(destinationVertex) + " : "+ distances[destinationVertex] + " miles");
            }
            printPath(destinationVertex, parents, stringList);
            
            System.out.print("On an average speed of 55 mph, it takes at least " + ((float) distances[destinationVertex] / 55) + " hours to reach the destination ");
            System.out.println("with a consumption of " + ((float) distances[destinationVertex] / mileage) + " gallons");
        }
    }

    private static void printPath(int currentVertex, int[] parents, List<String> stringList) {
        if (currentVertex == NO_PARENT) {
            return;
        }
        printPath(parents[currentVertex], parents, stringList);

        String currentCity = stringList.get(currentVertex);
        currentCity = currentCity.toLowerCase().replace (" ","");
        //System.out.print(currentCity + " -> ");
        shortest_path.append(currentCity);
        path.append(currentCity);
        shortest_path.append(temperature.fetch_display(" ", currentCity, 1));
        if (!currentCity.equals(endcity)) {
            shortest_path.append(",");
            path.append(",");
        }
//        System.out.println("In Dijk: " + currentCity);

    }
    
    
    public static String return_shortest_path() {
    	return shortest_path.toString();
    }
    public static String return_path() {
        return path.toString();
    }
}


