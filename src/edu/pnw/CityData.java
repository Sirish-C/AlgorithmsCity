package edu.pnw;
import java.util.LinkedHashMap;
import java.util.List;

//import cities.Main.WeatherData;

public class CityData {
    public final LinkedHashMap<String, WeatherData> weatherData = new LinkedHashMap<>();
    public List<Integer> distanceData;
    public List<Integer> seaLeveldata;

    public LinkedHashMap<String, WeatherData> getWeatherData() {
        return weatherData;
    }

    public List<Integer> getDistanceData() {
        return distanceData;
    }

    public void setDistanceData(List<Integer> distanceData) {
        this.distanceData = distanceData;
    }
    
     public List<Integer> getseaLeveldata() {
        return seaLeveldata;
    }
    public void setseaLeveldata(List<Integer> seaLeveldata) {
        this.seaLeveldata = seaLeveldata;
    }
}
