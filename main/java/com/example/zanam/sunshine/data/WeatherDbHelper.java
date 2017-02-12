package com.example.zanam.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zanam.sunshine.data.WeatherContract;

public class WeatherDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "weather.db";


    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE "+ WeatherContract.LocationEntry.TABLE_NAME + " (" +
                WeatherContract.LocationEntry._ID + " INTEGER PRIMARY KEY, " +
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                WeatherContract.LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                WeatherContract.LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                WeatherContract.LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL " +
                " ) ";

        db.execSQL(SQL_CREATE_LOCATION_TABLE);

        final String SQL_CREATE_WEATHER_TABLE ="CREATE TABLE "+ WeatherContract.WeatherEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                WeatherContract.WeatherEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                // the ID of the location entry associated with this weather data
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +

                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL," +

                " FOREIGN KEY (" + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                WeatherContract.LocationEntry.TABLE_NAME + " ("+ WeatherContract.LocationEntry._ID + "), " +

                " UNIQUE ("+ WeatherContract.WeatherEntry.COLUMN_DATE + ", " +
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.WeatherEntry.TABLE_NAME);
        onCreate(db);

    }

    public static void main(String[] args) {

        final String SQL_CREATE_WEATHER_TABLE ="CREATE TABLE "+ WeatherContract.WeatherEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                WeatherContract.WeatherEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                // the ID of the location entry associated with this weather data
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +

                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL," +
                WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL," +

                " FOREIGN KEY (" + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                WeatherContract.LocationEntry.TABLE_NAME + " ("+ WeatherContract.LocationEntry._ID + "), " +

                " UNIQUE ("+ WeatherContract.WeatherEntry.COLUMN_DATE + ", " +
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        System.out.println(SQL_CREATE_WEATHER_TABLE);

    }
}
