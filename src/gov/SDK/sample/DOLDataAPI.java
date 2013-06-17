package gov.SDK.sample;

import govdata.api.GOVDataContext;
import govdata.api.GOVDataRequest;
import govdata.api.GOVDataRequestCallback;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DOLDataAPI extends ListActivity implements GOVDataRequestCallback {
    /** Called when the activity is first created. */
	
 // API Key and URL constants
	public final String API_KEY =  "";
	public final String SHARED_SECRET = "";
    public final String API_URI = "/v1";
    public final String API_HOST = "http://api.dol.gov";
    public final String API_DATA = "DolAgency/Agencies";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     
        
        // Instantiate context object
        GOVDataContext context = new GOVDataContext(API_KEY, SHARED_SECRET, API_HOST, API_URI);
        
      //Instantiate new request object. Pass the context var that contains all the API key info
        //Set this as the callback responder
		GOVDataRequest request = new  GOVDataRequest(this, context);
		
		// HashMap to store the arguments
		HashMap<String, String> args = new HashMap<String, String>(7);
		 
		// API method to cal
		//Build Hashtable arguments
		
		String method = API_DATA;
		//args.put("top", "20");
	   args.put("select", "Agency,AgencyFullName");
		//Make API call
		try {
			request.callAPIMethod(method, args);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    
    
    // Callback method called when error occurs
	public void GOVDataErrorCallback(String error) {
		//Show error on dialog
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Error");
    	alertDialog.setMessage(error);
    	alertDialog.show();
	}

	
	     // Callback method called when results are returned and parsed
		public void GOVDataResultsCallback(Map<String, String> results) {	
			// Create List of strings to populate the listview items
			List<String> display = new ArrayList<String>();
			
			// Iterate thru List of results. Add each field to the display List	
			 
		
			for (Entry<String, String> m : results.entrySet()) {
			
			  // If the developer does not call out specific key pair values create default values
				
				  display.add(m.getKey()+" - "+ m.getValue()); 
				     
				 
			}
			
			
				
	
	   
			// Set list adapter. 
	    	setListAdapter(new ArrayAdapter<String>(this,R.layout.webview,R.id.textview,display));
	    	
	    	// Enable text filtering
	    	ListView lv = getListView();
	    	lv.setTextFilterEnabled(true);

		}


		@Override
		public void GOVDataResultsCallback(String results) {
			List<String> dataresults = new ArrayList<String>();
		    dataresults.add(results);
		    String values = dataresults.toString();
		
			// Set list adapter. 
	    	setListAdapter(new ArrayAdapter<String>(this,R.layout.webview,dataresults));
	    	// Enable text filtering
	    	ListView lv = getListView();
	    	lv.setTextFilterEnabled(true);
		      
		
		}

  public void GOVDOLDataResultsCallback(List<Map<String, String>> results) {
	
	  List<String> display = new ArrayList<String>();
		
		// Iterate thru List of results. Add each field to the display List		
		for (Map<String, String> m : results) {
			if(null != m.get("Agency") && m.get("Agency")!= "")
			{
				display.add(m.get("Agency")+" - "+m.get("AgencyFullName"));
			}
		}
  	
		// Set list adapter. 
  	setListAdapter(new ArrayAdapter<String>(this,R.layout.dol_list,display));
  	
  	// Enable text filtering
  	ListView lv = getListView();
  	lv.setTextFilterEnabled(true);

	  
  }










	



 
}