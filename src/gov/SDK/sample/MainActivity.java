package gov.SDK.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);	

	/** Add one menu item for each View in our project */
	menu.add(0, 0, 0, "Department of Labor");

	return true;
	}


	
	
	public void showGOV(){
		Intent GOVDATA = new Intent(this, DOLDataAPI.class);
		startActivity(GOVDATA);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	/** Select statement to handle calls
	to specific menu items */
	switch (item.getItemId()) {
	case 0:
		showGOV();
	return true;


	}
	return false;
	
}
}
