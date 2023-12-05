package edu.pnw;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistanceDataManager {

     public String [] cityMap = new String[119];

	 public List<List<String>> loadDistanceData(String csvFilePath) throws IOException {
	        List<List<String>> distanceData = new ArrayList<>();

	        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
	            String line;

	           // Read the CSV file line by line
	            int i=0;
	            while ((line = br.readLine()) != null) {
	                // Split the line into columns using a comma as the delimiter
	                String[] columns = line.split(",");
	                cityMap[i] = columns[0].toLowerCase().replace(" ", "");

	                // Create a list to store the entire row data
	                List<String> rowData = new ArrayList<>();

	                // Add all columns to the list
	                for (String value : columns) {
	                    rowData.add(value.trim());
	                }

	                // Add the list to the main list
	                distanceData.add(rowData);
	                i++;
	            }
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }

	        return distanceData;
	    }
	 
	 public int getIndex(String city){ 
		 for(int i =0;i< cityMap.length;i++){ 
			 if(cityMap[i].equals(city.toLowerCase().replace(" ",""))){ 
				 return i; } 
			 } 
		 return -1; 
	 }
	 
	 public int[][] readCSV(String filePath) {
        int[][] adjacencyMatrix = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int size = 0;

            while ((line = br.readLine()) != null) {
                size++;
            }
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            adjacencyMatrix = new int[size][size];

            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                //System.out.println("line values "+values);
                for (int j = 0; j < size; j++) {
                    adjacencyMatrix[i][j] = Integer.parseInt(values[j]);
                }
                i++;
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return adjacencyMatrix;
    }
}
