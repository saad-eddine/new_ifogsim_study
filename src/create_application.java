
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.Application;
import org.fog.entities.FogBroker;
import org.fog.entities.Tuple;
import org.fog.application.AppEdge;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.application.AppLoop;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class create_application {
	
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
	
	public static void main(String[] args) {
		Log.printLine("Starting...");

		try {
			String appId = "APPLICATION MASTER-SLAVE"; // identifier of the application
			
			FogBroker broker = new FogBroker("broker");
			createApplication(appId, broker.getId());
			
			
			Log.printLine("VRGame finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
}
