
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.fog.utils.FogLinearPowerModel;
import org.fog.scheduler.StreamOperatorScheduler;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;

import java.util.LinkedList;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import java.util.Random;

public class create_fog_nodes {
	
	static int numOfFogDevices = 10;
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static Map<String, Integer> getIdByName = new HashMap<String, Integer>();
	
	static boolean CLOUD = false;
	
	static int numOfDepts = 4;
	static int numOfMobilesPerDept = 6;
	static double EEG_TRANSMISSION_TIME = 5.1;
	//static double EEG_TRANSMISSION_TIME = 10;	
	
	public static double getValue(double min, double max) {
		Random rn = new Random();
		return ( min + (max-min) * rn.nextDouble());
	}
	
	public static int getValue(int min, int max) {
		Random rn = new Random();
		return (int)( min + (max-min) * rn.nextDouble());
	}
	
	private static void createFogDevices() {
		System.out.println("Iniciando criação da Cloud");
		
		FogDevice cloud = createAFogDevice("cloud-1",2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
		
		System.out.println("Cloud criada com sucesso!!");

		cloud.setParentId(-1);
		fogDevices.add(cloud);
		getIdByName.put(cloud.getName(), cloud.getId());
		System.out.println(cloud.getName());
		
		for(int i=0;i<numOfFogDevices;i++){
			System.out.println("Criando FOG DEVICE "+ i + "...");
			FogDevice device = createAFogDevice("FogDevice-"+i, getValue(12000, 15000), getValue(4000, 8000), getValue(200, 300), getValue(500, 1000), 1, 0.01,	getValue(100,120), getValue(70, 75));
			device.setParentId(cloud.getId());
			device.setUplinkLatency(10);
			fogDevices.add(device);
			getIdByName.put(device.getName(), device.getId());
		}
	}
	
	private static FogDevice createAFogDevice(String nodeName, double mips,
			int ram, double upBw, double downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
			);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);
		
		
		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fogdevice.setLevel(level);
		return fogdevice;
	}
	

	private static Application createApplication(String appId, int brokerId){
		Application application = Application.createApplication(appId, brokerId);
		application.addAppModule("MasterModule", 10);
		application.addAppModule("WorkerModule-1", 10);
		application.addAppModule("WorkerModule-2", 10);
		application.addAppModule("WorkerModule-3", 10);
		
		application.addAppEdge("Sensor", "MasterModule", 3000, 500, "Sensor", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("MasterModule", "WorkerModule-1", 100, 1000, "Task-1", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("MasterModule", "WorkerModule-2", 100, 1000, "Task-2", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("MasterModule", "WorkerModule-3", 100, 1000, "Task-3", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-1", "MasterModule",20,	50, "Response-1", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-2", "MasterModule",20,	50, "Response-2", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-3", "MasterModule",20,	50, "Response-3", Tuple.DOWN, AppEdge.MODULE);
		
		application.addAppEdge("MasterModule", "Actuators", 100, 50, "OutputData", Tuple.DOWN, AppEdge.ACTUATOR);
		
		application.addTupleMapping("MasterModule", " Sensor ", "Task-1", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", " Sensor ",	"Task-2", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", " Sensor ",	"Task-3", new FractionalSelectivity(0.3));
		application.addTupleMapping("WorkerModule-1", "Task-1", "Response-1", new FractionalSelectivity(1.0));
		application.addTupleMapping("WorkerModule-2", "Task-2", "Response-2", new FractionalSelectivity(1.0));
		application.addTupleMapping("WorkerModule-3", "Task-3", "Response-3", new FractionalSelectivity(1.0));
		application.addTupleMapping("MasterModule", "Response-1","OutputData", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", "Response-2","OutputData", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", "Response-3","OutputData", new FractionalSelectivity(0.3));
		
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("Sensor");add("MasterModule");add("WorkerModule-1");add("MasterModule");add("Actuator");}});
		final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("Sensor");add("MasterModule");add("WorkerModule-2");add("MasterModule");add("Actuator");}});
		final AppLoop loop3 = new AppLoop(new ArrayList<String>(){{add("Sensor");add("MasterModule");add("WorkerModule-3");add("MasterModule");add("Actuator");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);add(loop3);}};
		application.setLoops(loops);
		return application;
	}
	
	public static void listFogDevices (){
		System.out.println("Lista de FogDevices existentes!");
		for ( int i=0; i < numOfFogDevices; i++) {
			System.out.println("----------------------------------");
			System.out.println("Id do FogDevice: " + fogDevices.get(i).getId() );
			System.out.println("Nome do FogDevice: " + fogDevices.get(i).getName() );
			System.out.println("Host do FogDevice: ");
			System.out.println("	ID: " + fogDevices.get(i).getHost().getId());
			System.out.println("	RAM: " + fogDevices.get(i).getHost().getRam());
			System.out.println("	Storage: " + fogDevices.get(i).getHost().getStorage());
			System.out.println("Consumo de energia do FogDevice: " + fogDevices.get(i).getEnergyConsumption() );
			System.out.println("DownlinkBandwidth do FogDevice: " + fogDevices.get(i).getDownlinkBandwidth() );
			System.out.println("UplinkBandwidth do FogDevice: " + fogDevices.get(i).getUplinkBandwidth() );
			System.out.println("Parent ID do FogDevice: " + fogDevices.get(i).getParentId() );
			System.out.println("UplinkLatency do FogDevice: " + fogDevices.get(i).getUplinkLatency());
			System.out.println("RatePerMips do FogDevice: " + fogDevices.get(i).getRatePerMips());
			System.out.println("Total Cost do FogDevice: " + fogDevices.get(i).getTotalCost());
		}
	}
	
	public static void main(String[] args) {
		Log.printLine("Starting...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "application_teste1"; // identifier of the application
			
			FogBroker broker = new FogBroker("broker");
			
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			
			//createFogDevices(broker.getId(), appId);
			createFogDevices();
			listFogDevices();
			
			Log.printLine("VRGame finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

}

