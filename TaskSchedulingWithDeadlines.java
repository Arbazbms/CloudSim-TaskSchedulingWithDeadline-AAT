
package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a datacenter with one host and run one
 * cloudlet on it.
 */
public class TaskSchedulingWithDeadlines {

	/** The cloudlet list. */
	private static float timeSlice = (float)8;
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	private static List<Vm> createVM(int userId, int vms) {

		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//VM Parameters
		long size = 10000; //image size (MB)
		int ram = 512; //vm memory (MB)
		int mips = 1000;
		long bw = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name
		
		//create VMs
		Vm[] vm = new Vm[vms];

		
		for(int i=0;i<vms;i++){
			vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
			//for creating a VM with a space shared scheduling policy for cloudlets:
			//vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());

			list.add(vm[i]);
		}

		return list;
	}


	private static List<Cloudlet> createCloudlet(int userId, int cloudlets){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		long length = 1000;
		long fileSize = 300;
		long deadline = 1000;
		long profit = 1000;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			Random r= new Random();
			Random r1= new Random();
			
			cloudlet[i] = new Cloudlet(i, length +r.nextInt(2000),deadline + r.nextInt(2000),profit + r.nextInt(2000), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}


	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample6...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 3;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at least one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			//Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			//Fourth step: Create VMs and Cloudlets and send them to broker
			vmlist = createVM(brokerId,6); //creating 6 vms
			cloudletList = createCloudlet(brokerId,10); // creating 10 cloudlets

			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);

			//Print the debt of each user to each datacenter
//			datacenter0.printDebts();
//			datacenter1.printDebts();

			Log.printLine("CloudSimExample6 finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into the list.
		//for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		//Another list, for a dual-core machine
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 2048; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); // This is our first machine

		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		); // Second machine


		//To create a host with a space-shared allocation policy for PEs to VMs:
		//hostList.add(
    	//		new Host(
    	//			hostId,
    	//			new CpuProvisionerSimple(peList1),
    	//			new RamProvisionerSimple(ram),
    	//			new BwProvisionerSimple(bw),
    	//			storage,
    	//			new VmSchedulerSpaceShared(peList1)
    	//		)
    	//	);

		//To create a host with a oportunistic space-shared allocation policy for PEs to VMs:
		//hostList.add(
    	//		new Host(
    	//			hostId,
    	//			new CpuProvisionerSimple(peList1),
    	//			new RamProvisionerSimple(ram),
    	//			new BwProvisionerSimple(bw),
    	//			storage,
    	//			new VmSchedulerOportunisticSpaceShared(peList1)
    	//		)
    	//	);


		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(){

		
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	private static void printProfit(List<Cloudlet> arr, int t) {
		int size = arr.size();
		int pes =0;
		float sum = 0;
		float burstTime[] = new float[size];
		float waitingTime[] = new float[size];
		float turnAroundTime[] = new float[size];
		float a[] = new float[size];
		Cloudlet cloudlet;
		Log.print("WIT PROFIT::::::::::::");
		int n = arr.size();
		   for (Cloudlet jb : arr) 
	        {
	            System.out.print("rr1 : "+ jb + " ");
	        }
        Collections.sort(arr,
                         new Comparator<Cloudlet>() {
							@Override
							public int compare(Cloudlet a, Cloudlet b) {
								System.out.println("PROFIT: "+a.getCloudProfit());
								return (int) (b.getCloudProfit() - a.getCloudProfit());
							}
						});
        for (Cloudlet jb : arr) 
        {
            System.out.print("rr : "+ jb + " ");
        }
  
        boolean result[] = new boolean[t];
        int job[] = new int[t];
        int sum1 = 0;
        for (int i = 0; i < n; i++) 
        {
            for (int j
                 = (int) Math.min(t - 1, arr.get(i).getCloudDeadline() - 1);
                 j >= 0; j--) {
                if (result[j] == false) 
                {
                    result[j] = true;
                    System.out.println(arr.get(i).getCloudletId());
                    job[j] = arr.get(i).getCloudletId();
                    sum1 += arr.get(i).getCloudProfit();
                    break;
                }
            }
        }
        System.out.println("Selected JOBS: ");
        for (int jb : job) 
        {
            System.out.print(jb + " ");
        }
        System.out.println();
        System.out.println("Total Profit: " + sum1);
       
        
        // printing
        
        String indent = "    ";
		DecimalFormat dft = new DecimalFormat("###.##");
		for(int i = 0; i<job.length; i++) {
			cloudlet = arr.get(i);
			//We get the cpu time for each cloudlet
			String cpuTime = dft.format(cloudlet.getActualCPUTime());
			float convertedCPUTime = (float) Double.parseDouble(cpuTime);
			burstTime[i] = convertedCPUTime; //burst time is equal to execution time.
		}
		for(int i=0; i<job.length; i++) {
			a[i] = burstTime[i];
		}
		for(int i=0; i<job.length; i++) {
			waitingTime[i] = 0;
		}
		do {
			for(int i=0; i<job.length; i++) {
				if(burstTime[i]>timeSlice) {
					burstTime[i] -= timeSlice;
					for(int j=0; j<job.length; j++) {
						if((j != i) && (burstTime[j] != 0)) {
							waitingTime[j] += timeSlice;
						}
					}
				}
				else {
					for(int j=0; j<job.length; j++) {
						if((j != i) && (burstTime[j] != 0)) {
							waitingTime[j] += burstTime[i];
						}
					}
					burstTime[i] = 0;
				}
			}
			sum = 0;
			for(int k=0; k<job.length; k++) {
				sum += burstTime[k];
			}	
		}while(sum != 0);
		for(int i=0; i<job.length; i++) {
			turnAroundTime[i] = waitingTime[i] + a[i];
		}
		
		Log.printLine("========== OUTPUT ==========");
		Log.print("Cloudlet \t Burst Time \t Waiting Time \t Turn Around Time");
		Log.printLine();
		Log.print("-------------------------------------------------------------------");
		for(int i=0; i<job.length; i++) {
			cloudlet = arr.get(i);
			pes = arr.get(i).getNumberOfPes();
			System.out.println("\n");
			System.out.println("Cloudlet: "+cloudlet.getCloudletId()+ "\t\t" +a[i]+ "\t\t" +waitingTime[i]+ "\t\t" +turnAroundTime[i]);
		}
		/* Average waiting and turn around time */
		float averageWaitingTime = 0;
		float averageTurnAroundTime = 0;
		for(int j=0; j<job.length; j++) {
			averageWaitingTime += waitingTime[j];
		}
		for(int j=0; j<job.length; j++) {
			averageTurnAroundTime += turnAroundTime[j];
		}
		System.out.println("Average Waiting Time on Total: " +(averageWaitingTime/size)+ "\nAverage Turn Around Time on Total: " +(averageTurnAroundTime/size));
		
       
	}
	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	@SuppressWarnings("deprecation")
	private static void printCloudletList(List<Cloudlet> list) {
	
		int size = list.size();
		printProfit(list, 5);
		Cloudlet cloudlet;
		
		int pes =0;
		float sum = 0;
		float burstTime[] = new float[size];
		float waitingTime[] = new float[size];
		float turnAroundTime[] = new float[size];
		float a[] = new float[size];
		String indent = "    ";
		DecimalFormat dft = new DecimalFormat("###.##");
		for(int i = 0; i<size; i++) {
			cloudlet = list.get(i);
			//We get the cpu time for each cloudlet
			String cpuTime = dft.format(cloudlet.getActualCPUTime());
			float convertedCPUTime = (float) Double.parseDouble(cpuTime);
			burstTime[i] = convertedCPUTime; //burst time is equal to execution time.
		}
		for(int i=0; i<size; i++) {
			a[i] = burstTime[i];
		}
		for(int i=0; i<size; i++) {
			waitingTime[i] = 0;
		}
		do {
			for(int i=0; i<size; i++) {
				if(burstTime[i]>timeSlice) {
					burstTime[i] -= timeSlice;
					for(int j=0; j<size; j++) {
						if((j != i) && (burstTime[j] != 0)) {
							waitingTime[j] += timeSlice;
						}
					}
				}
				else {
					for(int j=0; j<size; j++) {
						if((j != i) && (burstTime[j] != 0)) {
							waitingTime[j] += burstTime[i];
						}
					}
					burstTime[i] = 0;
				}
			}
			sum = 0;
			for(int k=0; k<size; k++) {
				sum += burstTime[k];
			}	
		}while(sum != 0);
		for(int i=0; i<size; i++) {
			turnAroundTime[i] = waitingTime[i] + a[i];
		}
		
		Log.printLine("========== OUTPUT ==========");
		Log.print("Cloudlet \t Burst Time \t Waiting Time \t Turn Around Time");
		Log.printLine();
		Log.print("-------------------------------------------------------------------");
		for(int i=0; i<size; i++) {
			cloudlet = list.get(i);
			pes = list.get(i).getNumberOfPes();
			System.out.println("\n");
			System.out.println("Cloudlet: "+cloudlet.getCloudletId()+ "\t\t" +a[i]+ "\t\t" +waitingTime[i]+ "\t\t" +turnAroundTime[i]);
		}
		/* Average waiting and turn around time */
		float averageWaitingTime = 0;
		float averageTurnAroundTime = 0;
		for(int j=0; j<size; j++) {
			averageWaitingTime += waitingTime[j];
		}
		for(int j=0; j<size; j++) {
			averageTurnAroundTime += turnAroundTime[j];
		}
		System.out.println("Average Waiting Time on Total: " +(averageWaitingTime/size)+ "\nAverage Turn Around Time on Total: " +(averageTurnAroundTime/size));
		
		String indent1 = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent1 + "STATUS" + indent1 +
				"Data center ID" + indent1 + "VM ID" + indent1 + indent1 + "Time" + indent1 + "Start Time" + indent1 + "Finish Time" +indent1+"user id"+indent1);

//		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent1 + cloudlet.getCloudletId() + indent1 + indent1);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent1 + indent1 + cloudlet.getResourceId() + indent1 + indent1 + indent1 + cloudlet.getVmId() +
						indent1 + indent1 + indent1 + dft.format(cloudlet.getActualCPUTime()) +
						indent1 + indent1 + dft.format(cloudlet.getExecStartTime())+ indent1 + indent1 + indent1 + dft.format(cloudlet.getFinishTime())+indent1 +cloudlet.getUserId());
				
								
			}
		}
		

	}
}
