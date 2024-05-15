import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        //READ THE INPUT FILE
        String inputFile = "input1.txt";
        String outputFile = "output.txt";

        ArrayList<City> cities = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 3) {
                    int cityID = Integer.parseInt(parts[0]);
                    int xCoordinate = Integer.parseInt(parts[1]);
                    int yCoordinate = Integer.parseInt(parts[2]);
                    //store to arraylist
                    cities.add(new City(cityID, xCoordinate, yCoordinate));
                } else {
                    System.out.println("Invalid input format: " + line);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + e.getMessage());
        }

        // Nearest Neighbor algorithm for 2-tsp
        ArraylistHolder tours = nearestNeighbor(cities);
        List<City> tour1 = tours.getTour1();
        List<City> tour2 = tours.getTour2();
        //:::::TEST:::::
        System.out.println("--------------TEST-------------");
        System.out.println("tour1: - BEFORE 2-OPT");
        for (City city : tour1) {
            System.out.print(" , "+(city.getId()));
        }
        System.out.println();
        System.out.println("tour2:");
        for (City city : tour2) {
            System.out.print(" , "+(city.getId()));
        }
        System.out.println();
        int totalDistance = calculateTourDistance(tour1) + calculateTourDistance(tour2);
        System.out.println("total distance: "+totalDistance);

        // Apply 2-opt to improve the tours
        tour1 = twoOpt(tour1);
        tour2 = twoOpt(tour2);
        //TEST
        System.out.println("\n\ntour1: - AFTER 2-Opt Algorithm");
        for (City city : tour1) {
            System.out.print(" , "+(city.getId()));
        }
        System.out.println();
        System.out.println("tour2:");
        for (City city : tour2) {
            System.out.print(" , "+(city.getId()));
        }
        System.out.println();
        totalDistance = calculateTourDistance(tour1) + calculateTourDistance(tour2);
        System.out.println("total distance: "+totalDistance);

        // Write the output file
        writeSolution(outputFile, tour1, tour2);

    }

    // FUNCTION FOR CALCULATING DISTANCE BETWEEN TWO CITIES
    public static int calculateDistance(City c1, City c2) {
        double dist = Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
        return (int) Math.round(dist);
    }

    // NEAREST NEIGHBOR ALGORITHM
    public static ArraylistHolder nearestNeighbor(List<City> cities) {
        ArrayList<City> tour1 = new ArrayList<>();
        ArrayList<City> tour2 = new ArrayList<>();
        // Choose a random city to start
        Random random = new Random();
        City startCityTour1 = cities.get(random.nextInt(cities.size()));
        City startCityTour2 = cities.get(random.nextInt(cities.size()));
        tour1.add(startCityTour1);
        tour2.add(startCityTour2);// add the start city to the arraylist

        int visitedCount = 2; // Start with 1 for the initial city
        startCityTour1.setVisited(true);
        startCityTour2.setVisited(true);

        while (visitedCount < cities.size()) {
            City currentCityTour1 = tour1.get(tour1.size() - 1);
            City currentCityTour2 = tour2.get(tour2.size() - 1);
            City nearestCity = null;
            int minDistance = Integer.MAX_VALUE;
            int relativeDistance = 0;
            int distance1 = 0;
            int distance2 = 0;
            boolean setTour1=false;
            //check the nearest unvisited neighbor for the particular city
            for (City city : cities) {
                //checks if the current city is visited
                if (!city.isVisited()) {
                    //calculates the distance for salesman 1 and 2
                    distance1 = calculateDistance(currentCityTour1, city);
                    distance2 = calculateDistance(currentCityTour2, city);
                    //chooses the min distance calculated by salesman 1 and 2
                    relativeDistance = Math.min(distance2, distance1);
                    if (relativeDistance==distance1){
                        setTour1=true;
                    }
                    else {
                        setTour1=false;
                    }
                    if (relativeDistance < minDistance) {
                        minDistance = relativeDistance;
                        nearestCity = city;
                    }

                }
            }
            visitedCount=visitedCount+1;
            nearestCity.setVisited(true);
            // add the next nearest city to the tour
            if (setTour1) {
                tour1.add(nearestCity);
            } else {
                tour2.add(nearestCity);
            }
        }

        return new ArraylistHolder(tour1, tour2);
    }


    // 2-OPT ALGORITHM
    public static List<City> twoOpt(List<City> tour) {
        int n = tour.size();
        boolean improvement = true;
        int bestDistance = calculateTourDistance(tour);

        while (improvement) {
            improvement = false;
            for (int i = 1; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    List<City> newTour = new ArrayList<>(tour);
                    Collections.reverse(newTour.subList(i, j));
                    int newDistance = calculateTourDistance(newTour);
                    if (newDistance < bestDistance) {
                        tour = newTour;
                        bestDistance = newDistance;
                        improvement = true;
                        break;
                    }
                }
                if (improvement) {
                    break;
                }
            }
        }
        return tour;
    }


    // FUNCTION TO CALCULATE THE TOTAL DISTANCE OF THE TOUR
    public static int calculateTourDistance(List<City> tour) {
        int totalDistance = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            totalDistance += calculateDistance(tour.get(i), tour.get(i + 1));
        }
        return totalDistance;
    }

    // WRITE THE OUTPUT IN THE SPECIFIED FORMAT
    public static void writeSolution(String outputFile, List<City> tour1, List<City> tour2) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Calculate distance of tour 1
            int tour_1_Distance = calculateTourDistance(tour1);
            // Calculate distance of tour 2
            int tour_2_Distance = calculateTourDistance(tour2);
            int totalDistance = tour_1_Distance + tour_2_Distance;
            writer.println(totalDistance + " ");

            // Write tour for the first salesman
            writer.print(tour_1_Distance + " ");
            writer.println(tour1.size());
            for (City city : tour1) {
                writer.println(city.getId());
            }

            writer.println();

            // Write tour for the second salesman
            writer.print(tour_2_Distance + " ");
            writer.println(tour2.size());
            for (City city : tour2) {
                writer.println(city.getId());
            }

            writer.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
