import java.util.*;

public class display_temperatures {
	
	public Map<String, CityData> cityWeatherMap;
	public String formattedDateTime;
	String[] timeAndTimeZoneSplit = null;
	
	public display_temperatures(Map<String, CityData> cityWeatherMap, String formattedDateTime) {
		this.cityWeatherMap = cityWeatherMap;	
		this.formattedDateTime = formattedDateTime;
	}
	
	public String fetch_display(String fromCity, String toCity, int distance){
		
		CityData fromdata = cityWeatherMap.get(fromCity);
		CityData todata = cityWeatherMap.get(toCity);
		double from_sealevel_in_m;
		double to_sealevel_in_m;
		
		
		if(fromdata != null) {
			List<Integer> from_sea = fromdata.getseaLeveldata();
			if(from_sea != null) {
				from_sealevel_in_m = from_sea.get(0) / 3.281;
			}
		}
		
		if ((todata != null)){
			List<Integer> to_sea = todata.getseaLeveldata();
			if(to_sea != null) {
				to_sealevel_in_m = to_sea.get(0) / 3.281;
			}
		}
		
		

        if (toCity != null) {        	
        	// Create a list to store filtered weather entries
			int i =0;
			//System.out.println(i);
        	for (Map.Entry<String, WeatherData> weatherEntry : todata.getWeatherData().entrySet()) {

        	    String timestamp = weatherEntry.getKey();
        	    String[] dateTimeSplit = timestamp.split("T");
        	    String date = dateTimeSplit[0];
        	    String timeAndTimeZone = dateTimeSplit[1];
        	    
        	    if(timeAndTimeZone.contains("-")) {
        	    	timeAndTimeZoneSplit = timeAndTimeZone.split("-");
        	    }else if(timeAndTimeZone.contains("+")) {
        	    	timeAndTimeZoneSplit = timeAndTimeZone.split("\\+");
        	    }
        	    
        	    String time = timeAndTimeZoneSplit[0];
        	    String timezone = timeAndTimeZoneSplit[1];
        	    
        	    String[] formattedDateTimeSplit = formattedDateTime.split("T");
        	    String formattedDate = formattedDateTimeSplit[0];
        	    String formattedTime = formattedDateTimeSplit[1];

        	    WeatherData weatherData = weatherEntry.getValue();
        	    if (formattedDate.equals(date)) {
        	        // Check if the entry time is after formattedTime and before 23:00:00
        	        if (time.compareTo(formattedTime) >= 0 && time.compareTo("24:00:00") < 0) {
						i=i+1;
						if(i==1)
						{
							return (" [ Weather: " + weatherData.getWeather() +
									"; Sea Level: " + todata.getseaLeveldata() + " ft ]");
						}

        	        }
        	    }
        	}
        } else {
            System.out.println("Data not available for city: " + toCity);
        }
		return "";
	}
}