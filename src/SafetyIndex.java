
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.*;

class Temperature {
    String cityName ;
    LocalDateTime date ;
    float temperature;
    String condition;
    Temperature(String cityName , LocalDateTime date, float temperature , String condition){
        this.cityName = cityName.toLowerCase().replace(" ","");
        this.date = date;
        this.temperature = temperature;
        this.condition = condition;
    }
}

class Distance{
    static float [][] distance = new float[119][119];
    static String [] cityMap = new String[119];
    Distance(String path)throws Exception{
        load(path);
    }
    static void load(String path) throws Exception{
        // File path is passed as parameter
        File file = new File(path+"distance.csv");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        int i =0;
        while ((st = br.readLine()) != null){
            String [] arr = st.split(",");
            cityMap[i] = arr[0].toLowerCase().replace(" ","");
            for(int j =1;j<arr.length;j++){
                distance[i][j-1] = Float.parseFloat(arr[j]);
            }
            i++;
        }
    }
    public int getCityIndex(String city){
        for(int i =0;i<cityMap.length;i++){
            if(cityMap[i].equals(city)){
                return i;
            }
        }
        return -1;
    }
    public float getDistanceBetween(String cityA , String cityB){
        int cityIndexA = getCityIndex(cityA);
        int cityIndexB = getCityIndex(cityB);
        if(cityIndexA !=-1 && cityIndexB!=-1){
            return distance[cityIndexA][cityIndexB];
        }
        System.out.println("City A or City B not found!");
        return  -1;
    }
}
public class SafetyIndex {
    static  ArrayList<Temperature> weather =  new ArrayList<>();
    static ArrayList<String> cityConditionPolicy = new ArrayList<>();
    static ArrayList<Integer> score = new ArrayList<>();

    SafetyIndex(String path) throws Exception{
        load(path+"weather.csv");
        loadConditionPolicy(path+"SafetyIndexPolicy.csv");

    }

    public static  float getPoints(String conditions){
        for(int i =0;i<cityConditionPolicy.size();i++){
            if(cityConditionPolicy.get(i).toLowerCase().replace(" ","").equals(conditions.toLowerCase().replace(" ",""))){
                return (float)score.get(i);
            }
        }
        return 0f;
    }
    private static LocalDateTime getTime(String rawData){
        String [] date  = rawData.split("T")[0].split("-");
        String [] time = rawData.split("T")[1].split(":");
        return LocalDateTime.of(
                Integer.parseInt(date[0]),
                Integer.parseInt(date[1]),
                Integer.parseInt(date[2]),
                Integer.parseInt(time[0]),
                Integer.parseInt(time[1])
        );
    }

    private static void loadConditionPolicy(String path) throws Exception{
        // File path is passed as parameter
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        int i =0;
        while ((st = br.readLine()) != null){
            String [] rows  = st.split(",");
            cityConditionPolicy.add(rows[0].toLowerCase().replace(" ",""));
            score.add(Integer.parseInt(rows[1]));
            i++;
        }
    }
    private static void load(String path) throws Exception{
        // File path is passed as parameter
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        int i =0;
        while ((st = br.readLine()) != null){
            String [] rows  = st.split(",");
            String city = rows[0].toLowerCase().replace(" ","");
            LocalDateTime dateTime = getTime(rows[1]);
            String condition = rows[2];
            float temperature = Float.parseFloat(rows[3]);
            weather.add(new Temperature(
               city,
               dateTime,
               temperature,
               condition
            ));
            i++;
        }
    }

    public static Temperature getTemperature(String city, LocalDateTime time){
        for(int i =0;i<weather.size();i++){
            if(weather.get(i).cityName.equals(city.toLowerCase().replace(" ",""))){
                if(weather.get(i).date.isAfter(time)){
//                    System.out.println(weather.get(i). +" "+weather.get(i).date+" "+weather.get(i).condition+" "+weather.get(i).temperature);
                    return weather.get(i);
                }
            }

        }
        return null;
    }
    public static float getScoreForPath(String shortestPath, LocalDateTime StartTime , String DatasetPath )throws Exception{
        SafetyIndex temp = new SafetyIndex(DatasetPath);
        Distance distance = new Distance(DatasetPath);
        LocalDateTime time = StartTime;
        float speed = 30.3f;
        System.out.println("Weather Status at "+time);
        System.out.println();
        String [] path = shortestPath.split(",");
        int pathLength = path.length;
        float Score  = 0.0f;
        for(int i =0;i<path.length-1;i++){
            float dist = distance.getDistanceBetween(path[i] , path[i+1]);
            System.out.println("==========================================");
            System.out.println(path[i]+" --->"+path[i+1]);
            System.out.printf("%-20s %-15.2f miles%n","Distance" , dist);
            System.out.printf("%-20s %-15.2f hrs%n","Time" , ((dist/speed)));
            time = time.plusHours((long)(dist/speed));
            Temperature tempTime = getTemperature(path[i+1],time);
            System.out.printf("%-20s %-20s%n","Destination Time" , tempTime.date);
            System.out.printf("%-20s %-20s%n","Climate Condition" , tempTime.condition);
            System.out.printf("%-20s %-15.2f F%n","Temperature" , tempTime.temperature);
            float s = getPoints(tempTime.condition);
            System.out.printf("%-20s %-15.2f%n","SafetyScore" ,s);
            Score += s;
        }
        System.out.printf("%-25s %-15.2f%n","Final Safety Index :" ,Score/pathLength);
        return  Score/pathLength;
    }
}
