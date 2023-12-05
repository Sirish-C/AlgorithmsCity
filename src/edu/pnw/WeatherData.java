package edu.pnw;
public class WeatherData {
        public final String weather;
        public final int temperature;

        public WeatherData(String weather, int temperature) {
            this.weather = weather;
            this.temperature = temperature;
        }

        public String getWeather() {
            return weather;
        }

        public int getTemperature() {
            return temperature;
        }
    }