package com.example.gregoire_borel_2583863;

import java.util.ArrayList;
import com.google.android.gms.maps.model.LatLng;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GPSDatabase extends SQLiteOpenHelper
{
	public GPSDatabase(Context context)
	{
		super(context, "GPS tracking", null, 3);	
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		//	Creates a table with the coordinates and the time
        String CREATE_TRACKS_TABLE = "CREATE TABLE tracks (" + "time DATETIME DEFAULT CURRENT_TIMESTAMP," + "latitude REAL," + "longitude REAL)";
        db.execSQL(CREATE_TRACKS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
        db.execSQL("DROP TABLE IF EXISTS tracks");
        onCreate(db);
	}
	
	public void	clear()
	{
		this.getWritableDatabase().delete("tracks", "1", null);	//	Empties the database (and not delete!)
	}
	
	public void insertRow(Double latitude, Double longitude) 
	{
		SQLiteDatabase db = this.getWritableDatabase();	//	Returns a writable database
        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        
        db.insert("tracks", null, values);	//	Puts the values inside the table tracks
        db.close();	//	Close the 
	}

	//	Creates and returns a list of LatLng from the database
	public ArrayList<LatLng> getAllTracks()
	{
	    ArrayList<LatLng> trackList = new ArrayList<LatLng>();
        String selectQuery = "SELECT latitude, longitude FROM tracks";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);	//	Runs the provided query and returns a set of results
        if (cursor.moveToFirst())
        {
            do 
            {
            	LatLng	lat_lng = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
            	trackList.add(lat_lng);
            } while (cursor.moveToNext());
        }
        return (trackList);
    }
}