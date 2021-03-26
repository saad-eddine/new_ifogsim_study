import org.fog.application.AppEdge;
import org.fog.application.Application;
import org.fog.entities.Tuple;


public class sequential_unidirectional_dataflow_application {
	
	private static Application create_application(String appId, int brokerId){
		Application application = Application.createApplication(appId, brokerId);
		
		application.addAppModule("Module1", 10);
		application.addAppModule("Module2", 10);
		application.addAppModule("Module3", 10);
		application.addAppModule("Module4", 10);
		
		application.addAppEdge("Sensor", "Module1", 3000, 500, "Sensor", Tuple.UP, AppEdge.SENSOR);
		
		
		return application;
	}
}
