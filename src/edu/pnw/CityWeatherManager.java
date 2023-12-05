package edu.pnw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CityWeatherManager {
	public Map<String, CityData> loadWeatherData(String csvFilePath) throws IOException {
    Map<String, CityData> cityWeatherMap = new LinkedHashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
      String line;
      boolean headerSkipped = false;

      while ((line = br.readLine()) != null) {
        if (!headerSkipped) {
          // Skip the header line
          headerSkipped = true;
          continue;
        }

        String[] columns = line.split(",");
        String cityName = columns[0].trim();
        cityName = cityName.toLowerCase().replace (" ","");
        String timestamp = columns[1].trim();
        String weatherInfo = columns[2].trim();
        int temperature;

        try {
          temperature = Integer.parseInt(columns[3].trim());
        } catch (NumberFormatException e) {
          // Handle the case where temperature is not a valid integer
          System.out.println("Skipping invalid temperature for city " + cityName + " at timestamp " + timestamp);
          continue;
        }

        WeatherData weatherData = new WeatherData(weatherInfo, temperature);

        CityData cityData = cityWeatherMap.getOrDefault(cityName, new CityData());
        cityData.getWeatherData().put(timestamp, weatherData);

        cityWeatherMap.put(cityName, cityData);
      }
    }

    return cityWeatherMap;
  }

  

  public void combineData(Map<String, CityData> cityWeatherMap, List<List<String>> distanceData, List<List<String>> seaLevel) {
    // Process distance data
    for (List<String> rowData : distanceData) {
        if (rowData.size() < 2) {
            continue; // Skip rows with insufficient data
        }

        String cityName = rowData.get(0).trim();
        cityName = cityName.toLowerCase().replace(" ", "");
        CityData cityData = cityWeatherMap.getOrDefault(cityName, new CityData());

        List<Integer> distanceValues = rowData.subList(1, rowData.size()).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        cityData.setDistanceData(distanceValues);
        cityWeatherMap.put(cityName, cityData);
    }

    // Process sea level data (if available)
    if (seaLevel != null && !seaLevel.isEmpty()) {
        for (List<String> rowData : seaLevel) {
            if (rowData.size() < 2) {
                continue; // Skip rows with insufficient data
            }

            String cityName = rowData.get(0).trim();
            cityName = cityName.toLowerCase().replace(" ", "");
            CityData cityData = cityWeatherMap.getOrDefault(cityName, new CityData());

            List<Integer> seaLevelValues = rowData.subList(1, rowData.size()).stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            cityData.setseaLeveldata(seaLevelValues);
            cityWeatherMap.put(cityName, cityData);
        }
    }
}
}
