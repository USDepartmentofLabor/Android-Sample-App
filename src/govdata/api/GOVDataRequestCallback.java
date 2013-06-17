package govdata.api;

import java.util.List;
import java.util.Map;

public interface GOVDataRequestCallback {
	//Return results

	
	public void GOVDataResultsCallback(String results);
	

	//Error Callback
	public void GOVDataErrorCallback(String error);

	public void GOVDOLDataResultsCallback(List<Map<String, String>> objects);

	public void GOVDataResultsCallback(Map<String, String> objects);


	
}