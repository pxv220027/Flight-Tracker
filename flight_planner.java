import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.LinkedList;

//City class contains the name, the flights, and nextcity in the LinkedList
class City {
    String city;
    LinkedList<Flight> flights; 
    City nextCity;

    City(String city) {
        this.city = city;
        this.flights = new LinkedList<>(); 
        this.nextCity = null;
    }
}

// Flight class contains the destination, cost, time.
class Flight {
    String destination;
    double cost;
    int time;

    Flight(String destination, double cost, int time) {
        this.destination = destination;
        this.cost = cost;
        this.time = time;
    }
}

//InputOutputHandler handles reading and writing to files.
class InputOutputHandler {
    //readFlightFile reads the file and splits the data based on | and \ (\n).
    //It returns a list of the flights read.
    static List<List<String>> readFlightFile(String filename) throws IOException {
        List<List<String>> flights = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int numFlights = Integer.parseInt(reader.readLine().trim());
        for (int i = 0; i < numFlights; i++) {
            String[] flight = reader.readLine().trim().split("\\|");
            flights.add(Arrays.asList(flight));
            flights.add(Arrays.asList(flight[1], flight[0], flight[2], flight[3]));
        }
        reader.close();
        return flights;
    }
    //readRequestedFlie reads the file and splits the data based on | and \ (\n).
    //It returns a list of requestedFlights.
    static List<List<String>> readRequestedFlie(String filename) throws IOException {
        List<List<String>> requestedFlights = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int numRequests = Integer.parseInt(reader.readLine().trim());
        for (int i = 0; i < numRequests; i++) {
            String[] requestData = reader.readLine().trim().split("\\|");
            requestedFlights.add(Arrays.asList(requestData));
        }
        reader.close();
        return requestedFlights;
    }
    /*writeOutput writes to the output file the paths that one can take based on what 
    they requested and their preferences, such as time and cost*/
    static void writeOutput(String filename, List<List<Object>> flightPlans) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < flightPlans.size(); i++) {
            List<Object> plan = flightPlans.get(i);
            writer.write(String.format("Flight %d: %s, %s, (%s)%n", i + 1, plan.get(0), plan.get(1), plan.get(2).equals("T") ? "Time" : "Cost"));
            List<List<Object>> paths = (List<List<Object>>) plan.get(3);
            for (int j = 0; j < paths.size(); j++) {
                List<Object> pathInfo = paths.get(j);
                if (pathInfo.size() >= 3) {
                    List<String> path = (List<String>) pathInfo.get(0);
                    int time = (int) pathInfo.get(1);
                    double cost = (double) pathInfo.get(2);
                    writer.write(String.format("Path %d: %s. Time: %d Cost: %.2f%n", j + 1, String.join(" -> ", path), time, cost));
                } else {
                    writer.write(String.format("Path %d: %s%n", j + 1, pathInfo.get(0)));
                }
            }
            writer.write("\n");
        }
        writer.close();
    }
}

class Main {
    String inputFile;
    String requestFile;
    String outputFile;
    City rootCity;

    Main(String inputFile, String requestFile, String outputFile) {
        this.inputFile = inputFile;
        this.requestFile = requestFile;
        this.outputFile = outputFile;
        this.rootCity = null;
    }
    /*processFlights passes the file name to the readFlightFile() to 
    get the all the flights, and calls addFlight()*/
    void processFlights() throws IOException {
        List<List<String>> flights = InputOutputHandler.readFlightFile(inputFile);
        for (List<String> flight : flights) {
            String origin = flight.get(0);
            String destination = flight.get(1);
            double cost = Double.parseDouble(flight.get(2));
            int time = Integer.parseInt(flight.get(3));
            addFlight(origin, destination, cost, time);
        }
    }
    //addFlight calls addCity(),creates a flight, and adds the flight to the city
    void addFlight(String origin, String destination, double cost, int time) {
        City originCity = addCity(origin);
        Flight newFlight = new Flight(destination, cost, time);
        originCity.flights.add(newFlight);
    }
    
    /*addCity checks if the city exists and returns the existing city or creates a 
    new city and returns the newly created city*/
    City addCity(String cityName) {
        if (rootCity == null) {
            rootCity = new City(cityName);
            return rootCity;
        }
        City current = rootCity;
        while (current != null) {
            if (current.city.equals(cityName)) {
                return current;
            }
            if (current.nextCity == null) {
                current.nextCity = new City(cityName);
                return current.nextCity;
            }
            current = current.nextCity;
        }
        return null;
    }

    /*processRequestedFlights passes the file name to the readRequestedFlie() 
    to get the all the flights requested, and calls getAllPaths(), then adds the 
    paths recieved to the paths to the flightPlans*/
    void processRequestedFlights() throws IOException {
        List<List<Object>> flightPlans = new ArrayList<>();
        List<List<String>> requestedFlights = InputOutputHandler.readRequestedFlie(requestFile);
        for (List<String> request : requestedFlights) {
            String origin = request.get(0);
            String destination = request.get(1);
            String sorting = request.get(2);
            List<List<Object>> paths = getAllPaths(origin, destination, sorting);
            if (!paths.isEmpty()) {
                flightPlans.add(new ArrayList<>(Arrays.asList(origin, destination, sorting, paths.subList(0, Math.min(paths.size(), 3)))));
            } else {
                flightPlans.add(new ArrayList<>(Arrays.asList(origin, destination, sorting, Collections.singletonList(Collections.singletonList("No viable path found")))));
            }
        }
        InputOutputHandler.writeOutput(outputFile, flightPlans);
    }

    /*getAllPaths implements dfs to process all the flights and cities, and returns
    a list of all the viable paths sorted using time or cost*/
    private List<List<Object>> getAllPaths(String startCity, String destCity, String sort) {
        Stack<Object[]> stack = new Stack<>();
        stack.push(new Object[]{startCity, new ArrayList<>(Arrays.asList(startCity)), 0.0, 0});
        List<List<Object>> allPaths = new ArrayList<>();

        while (!stack.isEmpty()) {
            Object[] currentState = stack.pop();
            String currentCity = (String) currentState[0];
            List<String> path = (List<String>) currentState[1];
            double totalCost = (double) currentState[2];
            int totalTime = (int) currentState[3];

            if (currentCity.equals(destCity)) {
                allPaths.add(Arrays.asList(new ArrayList<>(path), totalTime, totalCost));
                continue;
            }

            City current = addCity(currentCity);
            if (current != null) {
                for (Flight currentFlight : current.flights) {
                    String nextCity = currentFlight.destination;
                    double nextCost = currentFlight.cost;
                    int nextTime = currentFlight.time;
                    if (!path.contains(nextCity)) {
                        List<String> newPath = new ArrayList<>(path);
                        newPath.add(nextCity);
                        stack.push(new Object[]{nextCity, newPath, totalCost + nextCost, totalTime + nextTime});
                    }
                }
            }
        }

        if (sort.equals("Cost")) {
            allPaths.sort(Comparator.comparingDouble(o -> (double) o.get(2)));
        } else {
            allPaths.sort(Comparator.comparingInt(o -> (int) o.get(1)));
        }

        return allPaths;
    }
    
    /*gets the file names from the command line, checks if all are there, and 
    processes the given data. */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Main flight_data.txt requested_flights.txt output.txt");
            System.exit(1);
        }

        String inputFile = args[0];
        String requestFile = args[1];
        String outputFile = args[2];

        Main main = new Main(inputFile, requestFile, outputFile);
        try {
            main.processFlights();
            main.processRequestedFlights();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}