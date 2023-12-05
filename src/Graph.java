import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.*;

class Graph {
    private int V;
    private List<int[]> edges;
    private int sourceVertex = 0; 
    private int destination = 0;
    private int[][] adjMat = null;
    private String startcity = null;
    private String endcity = null;
    private static float mileage;
    static display_temperatures temperature;
    StringBuilder shortest_path = new StringBuilder();
    public String[] cityList = {"MillCity-NV","Eureka-NV","Wells-NV","Howe-ID","Jackpot-NV","Montello-NV","Owyhee-NV","Eugene-OR","Gresham-OR","Hillsbboro-OR","Bend-OR","Beaverton-OR","Medford-OR","Johnday-OR","Roseburg-OR","Bakercity-OR","Salem-OR","Newport-OR","Ashland-OR","Riley-OR","Seattle-WA","Spokane-WA","Tacoma-WA","Vancouver-WA","Bellevue-WA","Kent-WA","Leavenworth-WA","Yakima-WA","Richland-WA","Pullman-WA","WestWendover-NV","LosAngeles-CA","SanDiego-CA","SanJose-CA","SanFrancisco-CA","Fresno-CA","Sacramento-CA","LongBeach-CA","Redding-CA","Bakersfield-CA","Bishop-CA","Irvine-CA","Darwin-CA","Bayswater-CA","Arvin-CA","Albuquerque-NM","LasCruces-NM","RioRancho-NM","SantaFe-NM","Roswell-NM","SaltLakeCity-UT","WestValleyCity-UT","Provo-UT","WestJordan-UT","Sandy-UT","Orem-UT","Ogden-UT","Missoula-MT","GreatFalls-MT","Bozeman-MT","Butte-SilverBow-MT","Helena-MT","Kalispell-MT","Almosa-CO","cortez-CO","GrandJunction-CO","Pueblo-CO","Lamar-CO","Fortcollins-CO","Aspen-CO","Telluride-CO","Montrose-CO","Prescott-AZ","Buckeye-AZ","Sedona-AZ","Tucson-AZ","Phoenix-AZ","Chinle-AZ","Kingman-AZ","Laramie-WY","Gillette-WY","RockSprings-WY","Sheridan-WY","GreenRiver-WY","Evanston-WY","Lander-WY","Rawlins-WY","Casper-WY","Cody-WY","Dubois-WY","JordanValley-OR","Rome-OR","Adrian-OR","Nyssa-OR","Owyhee-OR","Williston-ND","Dickinson-ND","Bowman-NE","Harrison-NE","Imperial-NE","Alliance-NE","Grant-NS","ELPaso-TX","Tribune-NV","SharonSprings-KS","Loeti-KS","Boise-ID","Meridian-ID","Nampa-ID","IdahoFalls-ID","Pocatello-ID","Caldwell-ID","Ketchum-ID","Chilly-ID","May-ID","Donnelly-ID","Buhl-ID","Kamaih-ID","Butte-MT"};

    public Graph(int vertices, int[][] adjacency_matrix, String startcity, String endcity, float mileage, display_temperatures temperature) {
    	
        V = vertices;
        edges = new ArrayList<>();
        this.adjMat = adjacency_matrix;
        this.startcity = startcity;
        this.endcity = endcity;
        this.mileage = mileage;
        this.temperature = temperature;
        
        
     // Iterate through the array and apply toLowerCase() to each element
        for (int i = 0; i < cityList.length; i++) {
        	cityList[i] = cityList[i].toLowerCase().replace (" ","");
        	//System.out.println(cityList[i]);
        }
    }

    public void addEdge(int u, int v, int w) {
        edges.add(new int[]{u, v, w});
    }
    
    public void find_and_addedges() {
    	
    	for (int i = 0; i < cityList.length; i++) {
            if (cityList[i].equals(startcity)) {
            	sourceVertex = i;
             //   break; // Exit the loop once the city is found
            }
            if(cityList[i].equals(endcity)){
            	destination = i;
            }
            if(sourceVertex != 0 && destination!= 0) {
            	break;
            } 
        }
    	
        for(int i=0;i<adjMat.length;i++){
        	for(int j=0;j<adjMat[i].length;j++){
		        if(adjMat[i][j] != 0 && !(sourceVertex == i && destination == j ||sourceVertex == j && destination == i)){
                    if(adjMat[i][j]<250){
                        addEdge(i, j, adjMat[i][j]);
                    }
                    
		        }
		    }
		    
		}
        bellmanFord(sourceVertex,destination);
    }

    public void bellmanFord(int src,int dest) {
        int[] dist = new int[V];
        int[] pred = new int[V];

        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        Arrays.fill(pred, -1);

        // Relax all edges |V| - 1 times
        for (int i = 0; i < V ; i++) {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                int weight = edge[2];
                if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v] ) {
                    dist[v] = dist[u] + weight;
                    pred[v] = u;
                }
            }
        }

        // Check for negative weight cycles
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int weight = edge[2];
            if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                System.out.println("Graph contains negative weight cycle");
                return;
            }
        }

        // Print the shortest paths
        System.out.println("Shortest paths from the source vertex:");
 
        printPath(src, dest, pred);
        System.out.println(" (Distance: " + dist[dest] + ")");
        System.out.print("On an average speed of 55 mph, it takes at least " + ((float) dist[dest] / 55) + " hours to reach the destination ");
        System.out.println("with a consumption of " + ((float) dist[dest] / mileage) + " gallons");

    }

    private void printPath(int src, int dest, int[] pred) {
        List<Integer> path = new ArrayList<>();
        int current = dest;
        while (current != -1) {
            path.add(0, current);
            current = pred[current];
        }
        System.out.print("Path from " +cityList[src] + " to " + cityList[dest] + ": ");
        for (int i = 0; i < path.size(); i++) {
            System.out.println(cityList[path.get(i)] + "-> ");
            shortest_path.append(cityList[path.get(i)]);
            temperature.fetch_display(" ", cityList[path.get(i)], 1);

            if (i < path.size() - 1) {
                shortest_path.append(",");
            }
        }
    }
    
    public String return_shortest_path() {
		return shortest_path.toString();
    }
}