import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.util.List;
import java.io.FileWriter;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
	public static String shortest_path = null;

    public static void main(String[] args) {
    	
    	String formattedDateTime;
        formattedDateTime = null;
        String datasetPath = "./Datasets/";
        String resultsPath = "./Results/";
        String weatherCsvFilePath = datasetPath+"weather.csv";
        String distanceCsvFilePath = datasetPath+"distance1.csv";
        String seaLevelCsvFilePath = datasetPath+"seaLevel.csv";
        String distanceWithoutHeaders = datasetPath+"distance_matrix_without_citynames.csv";
        String possiblePathCode = datasetPath+ "new_distance_matrics.csv";

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
        String userInput = sc.nextLine();
        System.out.println();
        
        if (userInput.isEmpty()) {
            System.out.println("Error: Please provide a non-empty input.");
            return;
        }
        
        try {
            // Parse user input into LocalDateTime
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime localDateTime = LocalDateTime.parse(userInput, inputFormatter);

            // Format the LocalDateTime into the desired output format
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            formattedDateTime = localDateTime.format(outputFormatter);

        } catch (Exception e) {
            System.out.println("Error: Unable to parse the input. Please provide a valid date and time format.");
            return;
        }
        
        
        System.out.print("Please enter the average mileage of your vehicle: (in mpg)");
        float mileage = sc.nextFloat();
        System.out.println();
        
        
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

       
        System.out.println("Please choose any one of the following: ");
        System.out.println("1. Display all possible paths from " + startcity + " to " + endcity);
        System.out.println("2. Run Dijkstra's Algorithm ");
        System.out.println("3. Run Bellman-Ford Algorithm");
        
        int user_selection = sc.nextInt();
        
        
        int[][] adjacencymatrix = distanceDataManager.readCSV(distanceWithoutHeaders);
        
		if(user_selection == 1) {
			int iCity = distanceDataManager.getIndex(startcity);
			int jCity = distanceDataManager.getIndex(endcity);
			
        	System.out.println("Distance between " + startcity + " and " + endcity + " is " + adjacencymatrix[iCity][jCity]);
        	System.out.print("Please specify how many miles additionally you would like to travel : " );
            int range = sc.nextInt();
            sc.nextLine();
            System.out.println();
            System.out.print("Displaying all available paths in the within  " + ( adjacencymatrix[iCity][jCity] + range ) + " mi");
            
           
            paths = new possible_paths_1(startcity, endcity, range, cityWeatherMap, temperature, mileage, possiblePathCode);
            List<List<String>> allpaths = paths.findAllPaths();
            
        }else if(user_selection == 2) {
        	
        	System.out.println(startcity);
        	
        	Dijkstras Dijkstra = new Dijkstras(startcity, endcity, temperature, adjacencymatrix, mileage);
        	shortest_path = Dijkstra.return_shortest_path();
//        	System.out.println("Dijkstra Shortest Path: " + shortest_path);
        	
        	
        }else if(user_selection == 3) {
        	
        	Graph bellman_ford = new Graph(120, adjacencymatrix, startcity, endcity, mileage, temperature);
            bellman_ford.find_and_addedges();
            shortest_path = bellman_ford.return_shortest_path();
//            System.out.println("Bellman Shortest Path: " + bellman_shortest);

            
        }else {
        	System.out.println("Error selection. Please try again !");
        	return;
        }
		
		if(shortest_path != null) {
			
			
//			SwingUtilities.invokeLater(() -> {
//	            double gasMileage = 0;
//	            DataReader dataReader = new DataReader("C:\\Users\\saini\\OneDrive\\Desktop\\New folder\\edu.pnw\\src\\main\\java\\edu\\pnw\\distance_matrics.csv");
//	            GraphVisualizer graphVisualizer = new GraphVisualizer(dataReader);
//	
//	            // Prompt user for input and perform visualization
//	            promptAndVisualize(dataReader, graphVisualizer);
//	        });

			
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputGraphical))) {
			    writer.write(shortest_path);
			} catch (IOException e) {
			    e.printStackTrace();
			}
			visualizeGraph();
			
		}
    }
    private static void visualizeGraph() {
        // Create an instance of the visualization class
        visualization visual = new visualization();

        // Call the main method of the visualization class
        visual.main(new String[]{});
    }
}