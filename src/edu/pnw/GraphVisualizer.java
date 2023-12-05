package edu.pnw;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;

public class GraphVisualizer {

    private DataReader dataReader;
    private Map<String, String> seaLevelData;

    // Constructor to receive DataReader instance
    public GraphVisualizer(DataReader dataReader) {
        this.dataReader = dataReader;
        this.seaLevelData = dataReader.getSeaLevelData();

    }

    public void visualizeFullGraph(double gasMileage) {
        // Create JUNG graph
        Graph<String, String> graph = createGraph(gasMileage);

        // Print graph information
        printGraphInformation(graph, gasMileage);

        // Create JUNG layout using SpringLayout
        Layout<String, String> layout = new SpringLayout<>(graph);

        // Create visualization viewer
        VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout, new Dimension(1500, 800));

        // Customize edge labels
        vv.getRenderContext().setEdgeLabelTransformer(e -> e.substring(e.lastIndexOf('-') + 1));

        // Customize vertex labels
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());

        // Customize vertex colors based on state ID
        vv.getRenderContext().setVertexFillPaintTransformer(vertex -> dataReader.getStateColors().get(vertex));

        showGraph(vv);

        // Animate the graph layout
        animateGraphLayout(vv);
    }

    public void visualizePath(String path, double gasMileage) {
        String[] pathCities = path.split(",");
        Graph<String, String> pathGraph = createGraphForPath(pathCities, gasMileage);
        visualizeGraph(pathGraph, pathCities, gasMileage);
    }



    private void visualizeGraph(Graph<String, String> pathGraph, String[] pathCities, double gasMileage) {
        Graph<String, String> graph = pathGraph;

        // Declare layout outside the if block
        Layout<String, String> layout;

        if (pathCities.length < 120) {
            layout = new ISOMLayout<>(graph);
        } else {
            layout = new KKLayout<>(graph);
        }

        // Adjust layout size
        Dimension layoutSize = new Dimension(1500, 800);
        layout.setSize(layoutSize);

        // Create visualization viewer
        VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout, layoutSize);

        // Customize edge labels
        vv.getRenderContext().setEdgeLabelTransformer(e -> e.substring(e.lastIndexOf('-') + 1));

        // Customize vertex labels
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());

        // Customize vertex colors based on state ID
        vv.getRenderContext().setVertexFillPaintTransformer(vertex -> dataReader.getStateColors().get(vertex));

        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().removeAll(); // Remove previous components
        frame.getContentPane().add(vv);
        frame.pack();  // Adjust the frame size
        frame.setLocationRelativeTo(null);  // Center the frame
        frame.setVisible(true);

        // Animate the graph layout
        ScalingControl scaler = new CrossoverScalingControl();
        DefaultModalGraphMouse<String, String> graphMouse = new DefaultModalGraphMouse<>();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING); // Set the default mode
        vv.setGraphMouse(graphMouse);
        vv.getRenderContext().setMultiLayerTransformer(vv.getRenderContext().getMultiLayerTransformer());
        vv.scaleToLayout(scaler);
    }
    private Graph<String, String> createGraphForPath(String[] pathCities, double gasMileage) {
        Graph<String, String> graph = new DirectedSparseGraph<>();

        for (int i = 0; i < pathCities.length - 1; i++) {
            String cityName = pathCities[i];
            String neighborCity = pathCities[i + 1];
            cityName = cityName.trim().toLowerCase().replaceAll("\\s", "");
            neighborCity = neighborCity.trim().toLowerCase().replaceAll("\\s", "");

            // Check if weather data exists for the city
            if (dataReader.getWeatherData().containsKey(cityName) && dataReader.getWeatherData().containsKey(neighborCity)) {
                Map<String, String> cityWeather = dataReader.getWeatherData().get(cityName);
                Map<String, String> neighborWeather = dataReader.getWeatherData().get(neighborCity);

                double distance = dataReader.getDistances().get(cityName).get(neighborCity);

                // Create edge label with distance and gas consumption
                double gasConsumption = Math.round((distance / gasMileage) * 100.0) / 100.0;
                String weatherStatus = neighborWeather.containsKey("WeatherCondition") ? neighborWeather.get("WeatherCondition") : "N/A";
                String temperature = neighborWeather.get("Temperature");

                String edgeLabel = cityName + "-" + neighborCity + "-" + distance + "m" + "," + gasConsumption + "g" + "," + weatherStatus + "," + temperature + "f";

                // Add directed edge
                graph.addEdge(edgeLabel, cityName, neighborCity, EdgeType.DIRECTED);
            } else if (!dataReader.getWeatherData().containsKey(cityName)) {
                //System.out.println("Weather data not available for city: " + cityName);
            } else if (!dataReader.getWeatherData().containsKey(neighborCity)) {
                //System.out.println("Weather data not available for city: " + neighborCity);
            }
        }

        // Visualize the graph after creating it
        visualizeText(graph, gasMileage, pathCities);

        return graph;
    }

    private void visualizeText(Graph<String, String> graph, double gasMileage, String[] pathCities) {
        System.out.println("Graph Visualization:");

        double totalGasConsumption = 0.0;
        double totalDistanceCovered = 0.0;
        double totalTimeTaken = 0.0; // Variable to accumulate total time taken

        for (int i = 0; i < pathCities.length - 1; i++) {
            String vertex = pathCities[i].trim().toLowerCase().replaceAll("\\s", "");

            Collection<String> successors = graph.getSuccessors(vertex);

            for (String neighbor : successors) {
                String edgeName = vertex + "-" + neighbor;

                // Check if the edge is directed (forward connection)
                if (graph.findEdge(vertex, neighbor) != null) {
                    double distance = dataReader.getDistances().get(vertex).get(neighbor);

                    System.out.println("City: " + vertex);
                    System.out.println("------------------------------------------------------> " + neighbor + " | Distance: " + distance + " miles");

                    // Check if weather data exists for the connected city
                    if (dataReader.getWeatherData().containsKey(neighbor)) {
                        Map<String, String> neighborWeather = dataReader.getWeatherData().get(neighbor);

                        // Print temperature information for the connected city
                        System.out.println("    Weather Information:");
                        String weatherStatus = neighborWeather.containsKey("WeatherCondition") ? neighborWeather.get("WeatherCondition") : "N/A";
                        String temperature = neighborWeather.get("Temperature");
                        Map<String, String> neighbor_datetime = dataReader.getWeatherData().get(vertex);
                        String datetime = neighbor_datetime.get("DateTime");

                        System.out.println("WeatherCondition: " + weatherStatus);
                        System.out.println("Temperature: " + temperature + " F");
                        System.out.println("DateTime: " + datetime);

                        // Print temperature, unit, and sea_level information
                        String seaLevel = seaLevelData.containsKey(neighbor) ? seaLevelData.get(neighbor) : "N/A";
                        System.out.println("  Sea Level: " + seaLevel);

                        // Print gas consumption information for the connected city
                        double gasConsumption = distance / gasMileage;
                        System.out.println("    Gas Consumption: " + gasConsumption + " gallons");

                        // Update total gas consumption, distance, and time taken
                        totalGasConsumption += gasConsumption;
                        totalDistanceCovered += distance;
                        totalTimeTaken += (distance / 60); // Assuming speed limit of 60 mph
                    } else {
                        System.out.println("    No weather data available for the connected city. (Check city name matching)");
                    }

                    System.out.println(); // Add a new line between connected cities
                }
            }
            System.out.println(); // Add a new line between cities
        }

        // Print total gas usage, distance covered, and time taken
        System.out.println("Total Gas Usage: " + totalGasConsumption + " gallons");
        System.out.println("Total Distance Covered: " + totalDistanceCovered + " miles");
        System.out.println("Total Time Taken: " + totalTimeTaken + " hours");
    }


    private Graph<String, String> createGraph(double gasMileage) {
        Graph<String, String> graph = new SparseGraph<>();

        for (String cityName : dataReader.getCities().keySet()) {
            graph.addVertex(cityName);
        }

        for (Map.Entry<String, Map<String, Double>> entry : dataReader.getDistances().entrySet()) {
            String cityName = entry.getKey();
            Map<String, Double> cityDistances = entry.getValue();

            if (cityDistances != null) {
                for (Map.Entry<String, Double> neighborEntry : cityDistances.entrySet()) {
                    String neighborCity = neighborEntry.getKey();
                    double distance = neighborEntry.getValue();

                    if (dataReader.getWeatherData().containsKey(neighborCity)) {
                        Map<String, String> neighborWeather = dataReader.getWeatherData().get(neighborCity);

                        double gasConsumption = Math.round((distance / gasMileage) * 100.0) / 100.0;
                        String weatherStatus = neighborWeather.containsKey("WeatherCondition") ? neighborWeather.get("WeatherCondition") : "N/A";

                        String temperature = neighborWeather.get("Temperature");
                        String edgeLabel = cityName + "-" + neighborCity + "-" + distance + "m" + "," + gasConsumption + "g" + "," + weatherStatus + "," + temperature + "f";
                        String edge_distance = cityName + "-" + neighborCity + "-" + distance;

                        boolean edgeExists = false;
                        for (String existingEdge : graph.getEdges()) {
                            if (existingEdge.startsWith(cityName + "-" + neighborCity) || existingEdge.startsWith(neighborCity + "-" + cityName)) {
                                edgeExists = true;
                                break;
                            }
                        }

                        if (!edgeExists && distance > 0 && distance <= 200) {
                            graph.addEdge(edgeLabel, cityName, neighborCity, EdgeType.UNDIRECTED);
                        }
                    }
                }
            }
        }

        return graph;
    }

    private void printGraphInformation(Graph<String, String> graph, double gasMileage) {
        System.out.println("Graph Information:");

        for (String vertex : graph.getVertices()) {
            System.out.println("City: " + vertex);

            Collection<String> neighbors = graph.getNeighbors(vertex);
            for (String neighbor : neighbors) {
                String edgeName = vertex + "-" + neighbor;

                double distance = dataReader.getDistances().get(vertex).get(neighbor);
                System.out.println("  Connected to " + neighbor + " | Distance: " + distance + " miles");

                if (dataReader.getWeatherData().containsKey(neighbor)) {
                    Map<String, String> neighborWeather = dataReader.getWeatherData().get(neighbor);

                    String weatherStatus = neighborWeather.containsKey("WeatherCondition") ? neighborWeather.get("WeatherCondition") : "N/A";
                    String temperature = neighborWeather.get("Temperature");
                    String datetime = neighborWeather.get("DateTime");

                    System.out.println("WeatherCondition :"+ weatherStatus);
                    System.out.println("Temperature :"+ temperature);
                    System.out.println("DateTime :"+ datetime);

                    System.out.println("    Gas Consumption: " + (distance / gasMileage) + " gallons");
                } else {
                    System.out.println("    No weather data available for the connected city. (Check city name matching)");
                }


                System.out.println();
            }
        }
    }

    private void showGraph(VisualizationViewer<String, String> vv) {
        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void animateGraphLayout(VisualizationViewer<String, String> vv) {
        ScalingControl scaler = new CrossoverScalingControl();
        DefaultModalGraphMouse<String, String> graphMouse = new DefaultModalGraphMouse<>();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(graphMouse);
        vv.getRenderContext().setMultiLayerTransformer(vv.getRenderContext().getMultiLayerTransformer());

        Timer timer = new Timer(100, e -> {
            scaler.scale(vv, 1.03f, vv.getCenter());
            vv.repaint();
        });

        timer.setRepeats(true);
        timer.start();
    }
}