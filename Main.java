import java.io.*;
import java.util.*;

public class Main {

    private static final int MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 1000;
    private static final int MAX_ITERATIONS = 10000;
    private static final double INITIAL_TEMPERATURE = 10000;
    private static final double COOLING_RATE = 0.995;
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
        System.out.println();
        int totalDistance = calculateTourDistance(tour1) + calculateTourDistance(tour2);
        System.out.println("total distance: "+totalDistance);


        tour1=simulatedAnnealing(tour1);
        tour2=simulatedAnnealing(tour2);

        // Apply 2-opt to improve the tours
        long l = System.currentTimeMillis();
        tour1 = twoOpt(tour1);
        tour2 = twoOpt(tour2);
        long e =System.currentTimeMillis();

        System.out.println("Execution time:"+(e-l/1000000));

        //TEST
        System.out.println("\n\ntour1: - AFTER 2-Opt Algorithm");

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
        City startCityTour1 = cities.get(0);
        City startCityTour2 = cities.get(cities.size()-1);
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

//
    // 2-OPT ALGORITHM
public static List<City> twoOpt(List<City> tour) {
    int n = tour.size();
    boolean improvement = true;
    int bestDistance = calculateTourDistance(tour);
    int iterationsWithoutImprovement = 0;

    while (improvement && iterationsWithoutImprovement < MAX_ITERATIONS_WITHOUT_IMPROVEMENT) {
        improvement = false;
        for (int i = 1; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int newDistance = calculateNewTourDistanceAfter2OptMove(tour, i, j, bestDistance);
                if (newDistance < bestDistance) {
                    perform2OptMove(tour, i, j);
                    bestDistance = newDistance;
                    improvement = true;
                    iterationsWithoutImprovement = 0; // Reset counter
                    break; // Exit inner loop
                }
            }
            if (improvement) {
                break; // Exit outer loop
            }
        }
        iterationsWithoutImprovement++;
    }
    return tour;
}

    public static int calculateNewTourDistanceAfter2OptMove(List<City> tour, int i, int j, int currentDistance) {
        int n = tour.size();

        // Get the cities involved in the 2-opt move
        City city1 = tour.get((i - 1 + n) % n); // Previous city of i, considering circular tour
        City city2 = tour.get(i);
        City city3 = tour.get(j);
        City city4 = tour.get((j + 1) % n); // Next city of j, considering circular tour

        // Calculate the distances of edges before and after the 2-opt move
        int oldDistance1 = calculateDistance(city1, city2);
        int oldDistance2 = calculateDistance(city3, city4);
        int newDistance1 = calculateDistance(city1, city3);
        int newDistance2 = calculateDistance(city2, city4);

        // Calculate the change in tour distance
        int distanceChange = (newDistance1 + newDistance2) - (oldDistance1 + oldDistance2);

        // Calculate the new tour distance
        return currentDistance + distanceChange;
    }
    public static List<City> simulatedAnnealing(List<City> tour) {
        double temperature = INITIAL_TEMPERATURE;
        List<City> currentTour = new ArrayList<>(tour);
        List<City> bestTour = new ArrayList<>(tour);
        int bestDistance = calculateTourDistance(tour);

        while (temperature > 1) {
            List<City> newTour = new ArrayList<>(currentTour);
            // Apply a random 2-opt move
            int i = 1 + (int) (Math.random() * (newTour.size() - 2));
            int j = i + 1 + (int) (Math.random() * (newTour.size() - i - 1));
            perform2OptMove(newTour, i, j);

            int currentDistance = calculateTourDistance(currentTour);
            int newDistance = calculateTourDistance(newTour);

            // Accept new tour if it's better or with a probability depending on the temperature
            if (acceptanceProbability(currentDistance, newDistance, temperature) > Math.random()) {
                currentTour = newTour;
                if (newDistance < bestDistance) {
                    bestTour = newTour;
                    bestDistance = newDistance;
                }
            }

            // Cool down
            temperature *= COOLING_RATE;
        }

        return bestTour;
    }

    // Acceptance probability function for simulated annealing
    public static double acceptanceProbability(int currentDistance, int newDistance, double temperature) {
        if (newDistance < currentDistance) {
            return 1.0;
        }
        return Math.exp((currentDistance - newDistance) / temperature);
    }

    // Function to perform a 2-opt move on the tour
    public static void perform2OptMove(List<City> tour, int i, int j) {
        while (i < j) {
            Collections.swap(tour, i, j);
            i++;
            j--;
        }
    }

    public static int calculateTourDistance(List<City> tour) {
        int totalDistance = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            totalDistance += calculateDistance(tour.get(i), tour.get(i + 1));
        }
        totalDistance += calculateDistance(tour.get(0), tour.get(tour.size() - 1));
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
