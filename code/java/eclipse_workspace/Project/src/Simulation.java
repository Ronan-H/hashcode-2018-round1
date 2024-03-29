import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Simulation {
	private int rows, cols;
	private int numVehicles;
	private int numRides;
	private int bonus;
	private int totalSteps;
	private List<Ride> rides;
	
	private Vehicle[] vehicles;
	private int currentStep;
	private List<Ride> availableRides;
	private ArrayList<ArrayList<Integer>> assignedRides;
	private int numRidesAssigned = 0;
	
	public Simulation(int rows, int cols, int numVehicles, int numRides, int bonus, int totalSteps, Ride[] rides) {
		this.rows = rows;
		this.cols = cols;
		this.numVehicles = numVehicles;
		this.numRides = numRides;
		this.bonus = bonus;
		this.totalSteps = totalSteps;
		
		// sort rides by their start time
		this.rides = new ArrayList<>(Arrays.asList(rides));
		
		// IMPORTANT: Only include this snippet for the metropolis input file (removes outliers)
		// Actually, don't include at all. Doesn't seem to help.
		/*
		for (int i = 0; i < this.rides.size(); ++i) {
			Ride r = this.rides.get(i);
			
			if (Math.abs(r.destCol - r.startCol) > cols / 3
			||  Math.abs(r.destRow - r.startRow) > rows / 3) {
				this.rides.remove(i);
			}
		}
		*/
		// -------------------------------------------------------------------
		
		this.rides.sort(new RideComparator());
		
		// init vehicles
		vehicles = new Vehicle[numVehicles];
		
		for (int i = 0; i < numVehicles; ++i) {
			vehicles[i] = new Vehicle();
		}
		availableRides = new ArrayList<Ride>();
		
		assignedRides = new ArrayList<ArrayList<Integer>>(numVehicles);
		
		for (int i = 0; i < numVehicles; ++i) {
			assignedRides.add(new ArrayList<Integer>());
			assignedRides.get(i).add(i + 1);
		}
		
		currentStep = 0;
	}
	
	public void startSimulation() {
		int i, j, k;
		for (currentStep = 0; currentStep <= totalSteps; ++currentStep) {
			// assign all free vehicles to any rides they can complete right now
			
			// update availableRides
			for (i = 0; i < availableRides.size(); ++i) {
				if (currentStep >= availableRides.get(i).mustFinish) {
					availableRides.remove(i);
					--i;
				}
			}
			
			for (i = 0; i < rides.size(); ++i) {
				Ride ride = rides.get(i);
				if (currentStep >= ride.mustStart && currentStep < ride.mustFinish) {
					Ride rideToAdd = rides.remove(i--);
					availableRides.add(rideToAdd);
				}
				else if (ride.mustStart > currentStep) {
					break;
				}
			}
			
			for (i = 0; i < availableRides.size(); ++i) {
				int smallestDist = Integer.MAX_VALUE;
				int smallestDistIndex = -1;
				
				for (j = 0; j < vehicles.length; ++j) {
					Vehicle vehicle = vehicles[j];
					if (vehicle.occupied) continue;
					
					int rideDist = availableRides.get(i).getRouteDist(vehicle);
					
					if (rideDist < smallestDist) {
						smallestDist = rideDist;
						smallestDistIndex = j;
					}
				}
				
				if (smallestDistIndex != -1) {
					Vehicle bestVehicle = vehicles[smallestDistIndex];
					
					if (availableRides.get(i).canCompleteRide(bestVehicle, currentStep)) {
						bestVehicle.doRide(availableRides.get(i));
						
						assignedRides.get(smallestDistIndex).add(availableRides.get(i).index);
						
						availableRides.remove(i);
						--i;
						
						++numRidesAssigned;
						break;
					}
				}
			}
			
			for (i = 0; i < vehicles.length; ++i) {
				vehicles[i].tick();
			}
		}
		
		// do output
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./output/out.txt")));
			
			for (i = 0; i < assignedRides.size(); ++i) {
				out.print(assignedRides.get(i).size() -1 + " ");
				for (j = 1; j < assignedRides.get(i).size(); ++j) {
					out.print(assignedRides.get(i).get(j) + " ");
				}
				out.println();
			}
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
