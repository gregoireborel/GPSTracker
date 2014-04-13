package com.example.gregoire_borel_2583863;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements LocationListener
{
	private GoogleMap m_googleMap;
	Button	m_actionButton;
	GPSDatabase m_GPSTracker;
	LocationManager	m_locationManager;
	ArrayList<String>	m_lengthOfTime;
	ArrayList<String>	m_speedList;
	float				m_totalSpeed;
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	@Override
    protected void 	onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.m_actionButton = (Button) findViewById(R.id.actionButton);
        this.m_actionButton.setText("Start");
        try	{	initilizeMap();	}	// Loading map
        catch (Exception e)	{	e.printStackTrace();	}
    }
	
	//	Initializes map and some variables
    private void 	initilizeMap()
    {
        if (this.m_googleMap == null)
        {
            this.m_googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            if (this.m_googleMap == null)	// Checks if map is created successfully or not
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            else
            {
	            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(53.347268, -6.258991)).zoom(10).build();	//	Centers map to O'Connell Street with a zoom of 10
	            this.m_googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));	//	Makes the animation of the camera's move
	            this.m_googleMap.setMyLocationEnabled(true);	//	Continuously displays the user's location	
	            this.m_GPSTracker = new GPSDatabase(this);
	            this.m_locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);  
	            this.m_speedList = new ArrayList<String>();
	            this.m_totalSpeed = 0;
	            this.m_lengthOfTime = new ArrayList<String>();
            }
        }
    }
    
    public	void	results(View v)
    {
    	if (this.m_actionButton.getText().toString() == "Stop")
    		Toast.makeText(this, "Tracking must be stopped!", Toast.LENGTH_SHORT).show();
    	else
    	{
    		ArrayList<LatLng> directionPoints = this.m_GPSTracker.getAllTracks();
        	if (directionPoints.isEmpty())	//	If there are not tracks, leaves
        		Toast.makeText(getApplicationContext(), "No data available!", Toast.LENGTH_SHORT).show();
        	else
        	{
        		ArrayList<String> distanceList = new ArrayList<String>();
				Location locationA = new Location("A");
				Location locationB = new Location("B");
				float	totalDistance = 0;
        		for (int i = 0; i < directionPoints.size(); i++)
        		{
        			if (i != directionPoints.size() - 1)
        			{
        				locationA.setLatitude(directionPoints.get(i).latitude);
        				locationA.setLongitude(directionPoints.get(i).longitude);
        				locationB.setLatitude(directionPoints.get(i + 1).latitude);
        				locationB.setLongitude(directionPoints.get(i + 1).longitude);
        				distanceList.add(Float.toString(locationA.distanceTo(locationB)));
        				totalDistance += locationA.distanceTo(locationB);
        			}
        		}       		
        		Intent myIntent = new Intent(this, ResultsActivity.class);
        		Bundle bundle = new Bundle();
        		bundle.putStringArrayList("speedList", this.m_speedList);	//	Adds average speed to the extra content
        		bundle.putFloat("totalSpeed", this.m_totalSpeed);	//	Adds total speed
        		bundle.putStringArrayList("distanceList", distanceList);	//	Adds distance list to the extra content
        		bundle.putFloat("totalDistance", totalDistance);	//	Adds total distance	
        		bundle.putStringArrayList("time", this.m_lengthOfTime);	//	Adds length of time to the extra content
        		myIntent.putExtras(bundle);
        		this.startActivity(myIntent);	//	Starts a new activity with a stored content
        	}      	
    	}
    }
    
    public	void	actionButton(View v)
	{
		if (this.m_actionButton.getText().toString() == "Start")
		{
			this.m_actionButton.setText("Stop");
			this.m_googleMap.clear();	//	Removes the previous drawings
			this.m_GPSTracker.clear();	//	Empties the database
			this.m_speedList.clear();
			this.m_totalSpeed = 0;
			this.m_lengthOfTime.clear();
			//	Here I offer two options: either you are using satellites or the Wi-Fi services to get user's location
			this.m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);	//	User's location is retrieve every 3 seconds
        	this.m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
		}
		else
		{
			this.m_actionButton.setText("Start");
    		this.m_locationManager.removeUpdates(this);	//	Stops the tracking
    		this.drawPath();	//	Draws the path
		}
	}   
    
    private	void	drawPath()
    {
    	ArrayList<LatLng> directionPoints = this.m_GPSTracker.getAllTracks();
    	if (directionPoints.isEmpty())	//	If there are not tracks, leaves
    		return ;
        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);	//	Customizes the line in red with a width of 5
        rectLine.addAll(directionPoints);	//	Adds all the tracks in the line that is going to be drawn
        this.m_googleMap.addPolyline(rectLine);	//	Adds the new line and draws it
        CameraPosition cameraPosition = new CameraPosition.Builder().target(directionPoints.get(0)).zoom(50).build();	//	Centers the camera on the first track
        this.m_googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    
    @Override
    protected void 	onResume()
    {
        super.onResume();
        //	Puts the location updates back
        this.m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);
    	this.m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);   
    }

    @Override
    protected void 	onPause()
    {
    	super.onPause();
        this.m_locationManager.removeUpdates(this);	//	Removes the updates when user is using another application
    }
    
    @Override
    protected void 	onStop()
    {
        super.onStop();
        this.m_GPSTracker.close();	//	Closes the database
    }
    
    //	Automatically called when location changed
    public 	void onLocationChanged(Location loc) 
	{	
		if (loc == null)	//	Filtering out null values
			return ;
		this.m_speedList.add(Float.toString((float) (loc.getSpeed() * 3.6)));	//	Adds location speed
		this.m_totalSpeed += loc.getSpeed() * 3.6;
		this.m_lengthOfTime.add(sdf.format(new Date(loc.getTime())));	//	Getting	location time
		this.m_GPSTracker.insertRow(loc.getLatitude(), loc.getLongitude());	//	Inserting in database the coordinates
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
			return true;
		return super.onOptionsItemSelected(item);
	}

	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub
	}
	
	public void onProviderEnabled (String provider)
	{
		// TODO Auto-generated method stub
	}
	
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		// TODO Auto-generated method stub
	}
}
