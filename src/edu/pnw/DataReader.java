package edu.pnw;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;



public class DataReader {

    private Map<String, Point> cities = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(DataReader.class.getName());

    private Map<String, Map<String, Double>> distances = new HashMap<>();
    private Map<String, Map<String, String>> weatherData = new HashMap<>();
    private Map<String, Color> stateColors = new HashMap<>();
    private Map<String, String> seaLevelData = new HashMap<>();
    private String distanceCsvFile; // New variable to store the distance CSV file path

    //private double totalTime = 0.0;

    // Create empty maps for cities, distances, weatherData, and stateColors



    public DataReader(String distanceCsvFile) {
        this.distanceCsvFile = distanceCsvFile;
    }

    public void readDataFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(distanceCsvFile))) {
            String line;
            boolean headerSkipped = false;
            String[] cityNames = null; // Store city names

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;

                    // Check if the row contains city names
                    if (line.contains(",")) {
                        cityNames = line.split(",");
                    } else {
                        // If not, use the first column as city names
                        cityNames = new String[]{line};
                        // Add a default city name at 0,0 position if it's empty
                        if (cityNames[0].trim().isEmpty()) {
                            cityNames[0] = "defaultcity";
                        }
                    }

                    continue; // Skip the header row
                }

                String[] parts = line.split(",");
                String originCity = parts[0].trim().toLowerCase().replaceAll("\\s", ""); // Convert to lowercase and remove spaces
                String stateId = extractStateId(originCity);

                cities.putIfAbsent(originCity, getRandomPoint());

                // Get or generate a color for the state based on the entire state ID
                Color stateColor = stateColors.computeIfAbsent(stateId, k -> getRandomColor());

                // Store state ID to color mapping
                stateColors.putIfAbsent(originCity, stateColor);

                // Iterate over distances and add them to the map
                for (int i = 1; i < parts.length; i++) {
                    String destinationCity = cityNames[i].trim().toLowerCase().replaceAll("\\s", ""); // Convert to lowercase and remove spaces
                    String distanceStr = parts[i].trim();

                    double distance = distanceStr.equals("0") ? Double.MAX_VALUE : Double.parseDouble(distanceStr);

                    cities.putIfAbsent(destinationCity, getRandomPoint());

                    distances.putIfAbsent(originCity, new HashMap<>());
                    distances.get(originCity).put(destinationCity, distance);

                    String destStateId = extractStateId(destinationCity);
                    Color destStateColor = stateColors.computeIfAbsent(destStateId, k -> getRandomColor());
                    stateColors.putIfAbsent(destinationCity, destStateColor);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void processWeatherData(String weatherCsvFile, String seaLevelCsvFile, String inputDate, String inputTime, int choice, String path) {
        if (choice == 1) {
            // Full Visualization
            processWeatherDataForFullVisualization(weatherCsvFile, seaLevelCsvFile, inputDate, inputTime);
        } else if (choice == 2) {
            // Path Visualization
            processWeatherDataForPathVisualization(weatherCsvFile, seaLevelCsvFile, inputDate, inputTime, path);
        } else {
            System.out.println("Invalid choice. Exiting...");
        }
    }


    private void processWeatherDataForFullVisualization(String weatherCsvFile, String seaLevelCsvFile, String inputDate, String inputTime) {
        try (BufferedReader br = new BufferedReader(new FileReader(weatherCsvFile))) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip the header row
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String cityName = parts[0].trim().toLowerCase().replaceAll("\\s", ""); // CityName-StateID


                    String dateTimeStr = parts[1].trim();
                    String dateStr = dateTimeStr.split("T")[0];

                    String timeStr = dateTimeStr.split("T")[1].split(":")[0] + ":00";
                    String weatherCondition = parts[2].trim().toLowerCase().replaceAll("\\s", "");
                    String temperatureStr = parts[3].trim();
                    // Check if the current row matches the input date and time
                    if (isMatchingDateTime(dateStr, timeStr, inputDate, inputTime)) {
                        // Add the weather data to the map
                        Map<String, String> cityWeather = weatherData.computeIfAbsent(cityName, k -> new HashMap<>());
                        cityWeather.put("DateTime", dateStr + " " + timeStr);
                        //System.out.println(cityWeather);
                        cityWeather.put("WeatherCondition", weatherCondition);
                        cityWeather.put("Temperature", temperatureStr + " F");
                    }
                }
            }


        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        processSeaLevelDataFromCSV(seaLevelCsvFile);
    }

    private void retrieveWeatherDataAndUpdateTime(String weatherCsvFile, String cityName, double travelTimeInHours, String currentDate, String currentTime) {
        // Calculate the updated date and time
        LocalDateTime updatedDateTime = calculateUpdatedDateTime(currentDate, currentTime, travelTimeInHours);

        // Format the updated date and time
        String updatedDateTimeStr = updatedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // Use the formatted date and time to store weather data
        processWeatherDataAndUpdateTime(weatherCsvFile, cityName, updatedDateTimeStr);
    }

    private void processWeatherDataAndUpdateTime(String weatherCsvFile, String cityName, String updatedDateTimeStr) {
        try (BufferedReader br = new BufferedReader(new FileReader(weatherCsvFile))) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip the header row
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String currentCityName = parts[0].trim().toLowerCase().replaceAll("\\s", "");

                    // Check if the current row matches the specified city
                    if (currentCityName.equals(cityName)) {
                        String dateTimeStr = parts[1].trim();
                        String dateStr = dateTimeStr.split("T")[0];
                        String timeStr = dateTimeStr.split("T")[1].split(":")[0] + ":00";

                        // Check if the current row's date and time match the desired updated date and time
                        if (isMatchingDateTime(dateStr, timeStr, updatedDateTimeStr)) {
                            // Add the weather data to the map
                            Map<String, String> cityWeather = weatherData.computeIfAbsent(cityName, k -> new HashMap<>());
                            cityWeather.put("DateTime", updatedDateTimeStr);
                            String weatherCondition = parts[2].trim().toLowerCase().replaceAll("\\s", "");
                            cityWeather.put("Temperature", parts[3].trim());
                            cityWeather.put("WeatherCondition", weatherCondition);

                            // Exit the loop after updating the weather data
                            break;
                        }
                    }
                }
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }


    private LocalDateTime calculateUpdatedDateTime(String currentDate, String currentTime, double travelTimeInHours) {
        // Parse the current date and time
        LocalDate currentDateObj = LocalDate.parse(currentDate);
        LocalTime currentTimeObj = LocalTime.parse(currentTime);

        // Calculate new time based on travel time and round up to the next hour
        long roundedHours = (long) Math.ceil(travelTimeInHours);
        LocalDateTime updatedDateTime = LocalDateTime.of(currentDateObj, currentTimeObj)
                .plusHours(roundedHours);

        // Check if the updated time exceeds 23:00
        if (updatedDateTime.toLocalTime().isAfter(LocalTime.parse("23:00"))) {
            // Increment date by 1 day and reset time to midnight
            updatedDateTime = updatedDateTime.plusDays(1).with(LocalTime.MIDNIGHT);
        }

        return updatedDateTime;
    }


    private boolean isMatchingDateTime(String dataDate, String dataTime, String updatedDateTime) {
        // Add your logic to check if the data date and time match the updated date and time
        return (dataDate + " " + dataTime).equals(updatedDateTime);
    }
    private boolean isMatchingDateTime(String dataDate, String dataTime, String inputDate, String inputTime) {
        // Add your logic to check if the data date and time match the input date and time
        return dataDate.equals(inputDate) && dataTime.equals(inputTime);
    }




    private void processWeatherDataForPathVisualization(String weatherCsvFile, String seaLevelCsvFile, String inputDate, String inputTime, String path) {
        String[] cities = path.split(",");
        String currentDate = inputDate;
        String currentTime = inputTime;

        // Process the rest of the cities
        for (int i = 0; i < cities.length - 1; i++) {
            String startCity = cities[i].trim().toLowerCase().replaceAll("\\s", "");
            String endCity = cities[i + 1].trim().toLowerCase().replaceAll("\\s", "");

            // Calculate travel time based on assumed speed limit (e.g., 50 miles per hour)
            double distance = getDistanceBetweenCities(startCity, endCity);
            double speedLimit = 60; // Adjust the speed limit as needed
            double travelTimeInHours = distance / speedLimit;

            // Print time travel for the current city pair
            String timeTravel = String.format("Time travel from %s to %s: %.2f hours", startCity, endCity, travelTimeInHours);
            //System.out.println(timeTravel);



            // Retrieve weather data for the end city with updated time and date
            retrieveWeatherDataAndUpdateTime(weatherCsvFile, startCity, travelTimeInHours, currentDate, currentTime);

            // Update current date and time for the next city
            Map<String, String> startCityWeather = weatherData.get(startCity);
            if (startCityWeather != null) {
                currentDate = startCityWeather.get("DateTime").split(" ")[0];
                currentTime = startCityWeather.get("DateTime").split(" ")[1];
                //System.out.println(currentDate);
                //System.out.println(currentTime);

            } else {
                // Handle the case where weather data is not available for the current city
                System.out.println("Weather data not available for " + startCity);
                break;
            }
        }

        //System.out.println("Total time for travel: " + totalTime + " hours");

        processSeaLevelDataFromCSV(seaLevelCsvFile);
    }


// Other methods remain the same





    private double getDistanceBetweenCities(String startCity, String endCity) {
        // Check if startCity and endCity exist in distances map


        Map<String, Double> startCityDistances = distances.get(startCity);
        if (distances.containsKey(startCity) && distances.containsKey(endCity)) {

            // Check if endCity exists in the distances for startCity
            if (startCityDistances.containsKey(endCity)) {
                // Retrieve the distance
                return startCityDistances.get(endCity);
            } else {
                // Handle the case where the distance is not available
                System.out.println("Distance not available for " + startCity + " to " + endCity);
            }
        }
        // Return a default distance or handle the situation as appropriate for your logic
        return 0.0; // You may want to adjust this default value
    }


    private void processSeaLevelDataFromCSV(String seaLevelCsvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(seaLevelCsvFile))) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip the header row
                }

                String[] parts = line.split(","); // Assuming tab-separated values


                String cityName = parts[0].trim().toLowerCase().replaceAll("\\s", ""); // CityName-StateID

                String seaLevelStr = parts[1].trim();

                // Store sea level data in your map or perform any other processing
                seaLevelData.put(cityName, seaLevelStr);

            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private String extractStateId(String city) {
        return city.substring(city.lastIndexOf('-') + 1).trim();
    }

    private Point getRandomPoint() {
        int x = (int) (Math.random() * 500) + 50; // Adjust the range as needed
        int y = (int) (Math.random() * 500) + 50;
        return new Point(x, y);
    }
    public Map<String, Color> getStateColors() {
        return stateColors;
    }
    public void setDistanceCsvFile(String distanceCsvFile) {
        this.distanceCsvFile = distanceCsvFile;
    }

    public Map<String, Map<String, String>> getWeatherData() {
        return weatherData;
    }
    public Map<String, Map<String, Double>> getDistances() {
        return distances;
    }
    public Map<String, String> getSeaLevelData() {
        return seaLevelData;
    }

    public Map<String, Point> getCities() {
        return cities;
    }





    private Color getRandomColor() {
        // Generate a random color
        return new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
    }

}