import java.util.*;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
	public static String shortest_path = null;
    public static String userInput= null;

    public static int user_selection;

    public static int getSeaLevel(List<List<String>> seaLevelData, int index){
        List<String> test = seaLevelData.get((index));
        return Integer.parseInt(test.get(1));
    }



    public static float[][] updateAdjacencyMatrix(float[][] mat){
        float[][] adjMAat = mat;
        try{
            String seaLevelCsvFilePath = "./Datasets/seaLevel.csv";
            DistanceDataManager distanceDataManager = new DistanceDataManager();
            SeaLevel seaLevel = new SeaLevel();
            List<List<String>> seaLeveldata = seaLevel.loadSeaLevel(seaLevelCsvFilePath);
            for(int i =0 ;i<adjMAat.length;i++){
                for(int j =0;j<adjMAat[i].length ;j++){
                    int s1 = getSeaLevel(seaLeveldata,i);
                    int s2 = getSeaLevel(seaLeveldata,j);
                    int diff = s1-s2;
                    if(diff <-100){
                        adjMAat[i][j] = (float) (adjMAat[i][j]*0.95);
                    }
                    if(diff>100){
                        adjMAat[i][j] = (float) (adjMAat[i][j]*1.05);
                    }
                }
            }}
        catch (IOException e) {
            e.printStackTrace();
        }
        return adjMAat;
    }
    public static void main(String[] args)throws Exception {
    	
    	String formattedDateTime;
        formattedDateTime = null;
        String datasetPath = "./Datasets/";
        String resultsPath = "./Results/";
        String weatherCsvFilePath = datasetPath+"weather.csv";
        String distanceCsvFilePath = datasetPath+"distance1.csv";
        String seaLevelCsvFilePath = datasetPath+"seaLevel.csv";
        String distanceWithoutHeaders = datasetPath+"distance_matrix_without_citynames.csv";
        String possiblePathCode = datasetPath+ "new_distance_matrics.csv";
        LocalDateTime localDateTime;        

        String outputGraphical = resultsPath+"output_graphical.txt";
        
        System.out.print("Please provide the name of your starting city and state in the specified format: Ex: (MillCity-NV) ");
        Scanner sc = new Scanner(System.in);
        String startcity = sc.nextLine();
        if(startcity != null) {
        	startcity = startcity.toLowerCase().replace (" ","");
        }
        System.out.println();
        
        System.out.print("Please provide your destination city in the same format: (City-State) ");
        String endcity = sc.nextLine();
        if(endcity != null) {
        	endcity = endcity.toLowerCase().replace (" ","");
        }
        System.out.println();
        
        System.out.print("Enter date and time of your travel commencement (yyyy-MM-dd HH:mm): ");
        userInput = sc.nextLine();
        System.out.println();
        
        if (userInput.isEmpty()) {
            System.out.println("Error: Please provide a non-empty input.");
            return;
        }
        
        try {
            // Parse user input into LocalDateTime
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            localDateTime = LocalDateTime.parse(userInput, inputFormatter);

            // Format the LocalDateTime into the desired output format
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            formattedDateTime = localDateTime.format(outputFormatter);

        } catch (Exception e) {
            System.out.println("Error: Unable to parse the input. Please provide a valid date and time format.");
            return;
        }

        float mileage = 30;
        
        
        //Declaration of Objects outside the try-catch block, ensuring its access across the function.
        
        CityWeatherManager cityWeatherManager = null;
        DistanceDataManager distanceDataManager = null;
        visualization visual = null;
        Map<String, CityData> cityWeatherMap = null;
        List<List<String>> distanceData = null;
        List<List<String>> seaLeveldata = null;
        display_temperatures temperature = null;
        possible_paths_1 paths;
        SeaLevel seaLevel;
        
        
        // Instantiate classes
        
        try {
            cityWeatherManager = new CityWeatherManager();
            distanceDataManager = new DistanceDataManager();
            seaLevel = new SeaLevel();

            // Load weather data
            cityWeatherMap = cityWeatherManager.loadWeatherData(weatherCsvFilePath);

            // Load distance data
            distanceData = distanceDataManager.loadDistanceData(distanceCsvFilePath);
            
            // Validation
            List<String> firstColumnValues = new ArrayList<>();
            for (List<String> row : distanceData) {
                if (!row.isEmpty()) {
                	String temp = row.get(0).toLowerCase().replace (" ","");
                    firstColumnValues.add(temp);
                }
            }
            
            if(!firstColumnValues.contains(startcity)) {
            	System.out.println("Start City " + startcity + " is not in our Time-Zone. Please try again !");
            	return;
            }else if(!firstColumnValues.contains(endcity)) {
            	System.out.println("End City " + endcity + " is not in our Time-Zone. Please try again !");
            	return;
            }

            // Load seaLevel data
            seaLeveldata = seaLevel.loadSeaLevel(seaLevelCsvFilePath);

            // Combine data
            cityWeatherManager.combineData(cityWeatherMap, distanceData, seaLeveldata);

            temperature = new display_temperatures(cityWeatherMap, formattedDateTime);

        } catch (IOException e) {
            e.printStackTrace();
        }


        do{
        System.out.println("Please choose any one of the following: ");
        System.out.println("1. Visualizing all cities");
        System.out.println("2. Shortest Path from " + startcity + " to " + endcity);
        System.out.println("3. Lowest gas consumption path from " + startcity + " to " + endcity);
        System.out.println("4. Safest Travel path from"+ startcity + " to " + endcity);
        System.out.println("5. Exit");

        user_selection = sc.nextInt();
        
        
        float[][] adjacencymatrix = distanceDataManager.readCSV(distanceWithoutHeaders);
        if(user_selection == 1 ){
            visualizeGraph(user_selection);
        }
		else if(user_selection == 2) {
            int range = 10;

            Dijkstras Dijkstra = new Dijkstras(startcity, endcity, temperature, adjacencymatrix, mileage, true);
            shortest_path = Dijkstra.return_shortest_path();
            System.out.println("Dijkstra Shortest Path: ");
            Dijkstra.display(shortest_path,adjacencymatrix);
            shortest_path = Dijkstra.return_path();

            System.out.println("The other paths are : ");
            paths = new possible_paths_1(startcity, endcity, range, cityWeatherMap, temperature, mileage, possiblePathCode);
            List<List<String>> otherPaths = paths.findPaths(Dijkstra.shortestDistance);
            for(int i=0 ;i<3;i++){
                try{
                    System.out.println("Path : "+paths.distancesPaths.get(i)+" Distance : "+paths.distances1[i]);
                }
                catch (Exception e){

                }

            }
        }else if(user_selection == 3) {
            float[][] updatedAdjacencyMatrix = updateAdjacencyMatrix(adjacencymatrix);
            Dijkstras Dijkstra1 = new Dijkstras(startcity, endcity, temperature, updatedAdjacencyMatrix, mileage,false);
            shortest_path = Dijkstra1.return_shortest_path();
            System.out.println("Path with less gasoline consumption : ");
            Dijkstra1.display(shortest_path,updatedAdjacencyMatrix);
            shortest_path = Dijkstra1.return_path();
            //System.out.println("Total Distance = "+ Dijkstra1.getDistance(adjacencymatrix,shortest_path));

        	
        	
        } else if(user_selection == 4) {
            paths = new possible_paths_1(startcity, endcity, 30, cityWeatherMap, temperature, mileage, possiblePathCode);
            List<List<String>> allpaths = paths.findAllPaths();
            SafetyIndex safetyIndex = new SafetyIndex(datasetPath);
            int maxPoints = 0;
            float path1_points = SafetyIndex.getScoreForPath(allpaths.get(0),localDateTime,55 ,datasetPath, false);
            float path2_points = SafetyIndex.getScoreForPath(allpaths.get(7),localDateTime,55 ,datasetPath, false);
            float path3_points = SafetyIndex.getScoreForPath(allpaths.get(9),localDateTime,55 ,datasetPath, false);
            System.out.println();
            if(path1_points>path2_points && path1_points>path2_points) System.out.println("Suggested Best Path ="+allpaths.get(0));
            else if(path2_points>path3_points && path2_points>path1_points) System.out.println("Suggested Best Path ="+allpaths.get(7));
            else if (path3_points>path1_points && path3_points>path2_points) System.out.println("Suggested Best Path ="+allpaths.get(9));
        }else{
        	System.out.println("Error selection. Please try again !");
        	return;
        }

        System.out.println("Do you want to visualize the map ? y/n : ");
        String decision = sc.next();
        if(decision.equalsIgnoreCase("y")){
            if(shortest_path != null) {

                visualizeGraph(user_selection);

            }
        }
		
		}while(user_selection != 5);
    }
    private static void visualizeGraph(int user_selection) {

        // Create an instance of the visualization class
        visualization visual = new visualization();

        // Call the main method of the visualization class
        if(user_selection ==1){
            shortest_path = "";
        }
        visual.get_data(shortest_path, userInput,user_selection);

    }
}