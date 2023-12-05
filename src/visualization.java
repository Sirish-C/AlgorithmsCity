import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class visualization {

    // Constants for file paths
    private static final String DATASET_PATH = "./Datasets/";
    private static final String WEATHER_CSV_FILE = DATASET_PATH +  "weather.csv";
    private static final String SEA_LEVEL_CSV_FILE = DATASET_PATH + "sealevelforallcities.csv";
    private static final String DISTANCE_CSV_FILE = DATASET_PATH + "distance_matrics.csv";
    private static final String OUTPUT_PATH = DATASET_PATH +  "output_graphical.txt";

    // Enum for visualization options
    private enum VisualizationOption {
        FULL_GRAPH,
        SPECIFIC_PATH
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            double gasMileage = 0;
            DataReader dataReader = new DataReader(DISTANCE_CSV_FILE);
            GraphVisualizer graphVisualizer = new GraphVisualizer(dataReader);

            // Prompt user for input and perform visualization
            promptAndVisualize(dataReader, graphVisualizer);
        });
    }

    private static void promptAndVisualize(DataReader dataReader, GraphVisualizer graphVisualizer) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the date (YYYY-MM-DD):");
        String inputDate = scanner.next();

        System.out.println("Enter the time (HH:mm):");
        String inputTime = scanner.next();

        System.out.println("Choose an option:");
        System.out.println("1. Visualize full graph");
        System.out.println("2. Visualize a specific path");
        int choice = scanner.nextInt();

        VisualizationOption visualizationOption = VisualizationOption.values()[choice - 1];

        if (visualizationOption == VisualizationOption.FULL_GRAPH) {
            System.out.println("Enter gas mileage:");
            double gasMileage = scanner.nextDouble();
            dataReader.readDataFromCSV();
            dataReader.processWeatherData(WEATHER_CSV_FILE, SEA_LEVEL_CSV_FILE, inputDate, inputTime, choice, "");
            graphVisualizer.visualizeFullGraph(gasMileage);
        } else if (visualizationOption == VisualizationOption.SPECIFIC_PATH) {
            System.out.println("Enter gas mileage:");
            double gasMileage = scanner.nextDouble();
            dataReader.readDataFromCSV();
            String pathCities = visualizePathFromFile(OUTPUT_PATH, gasMileage);
            dataReader.processWeatherData(WEATHER_CSV_FILE, SEA_LEVEL_CSV_FILE, inputDate, inputTime, choice, pathCities);
            graphVisualizer.visualizePath(pathCities, gasMileage);
        } else {
            System.out.println("Invalid choice. Exiting...");
        }

        scanner.close();
    }

    private static String visualizePathFromFile(String outputPath, double gasMileage) {
        StringBuilder pathCitiesString = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(outputPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] pathCitiesWithState = line.replace("[", "").replace("]", "").split(",");
                for (String cityStatePair : pathCitiesWithState) {
                    String[] pair = cityStatePair.split("-");
                    if (pair.length == 2) {
                        pathCitiesString.append(pair[0]).append("-").append(pair[1]).append(",");
                    } else {
                        System.out.println("Invalid format in the output file: " + cityStatePair);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add a dummy variable
        pathCitiesString.append("dummyVariable");

        return pathCitiesString.toString();
    }
}