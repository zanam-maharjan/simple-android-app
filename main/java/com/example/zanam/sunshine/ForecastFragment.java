package com.example.zanam.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    protected ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

            return super.onOptionsItemSelected(item);
        }

    private void updateWeather(){
        FetchWeatherTask weatherTask=new FetchWeatherTask();
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location=preferences.getString((getString(R.string.pref_location_key)),
                getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }


        @Override
        public View onCreateView (LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState){

             mForecastAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview,
                    new ArrayList<String>());

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
            listView.setAdapter(mForecastAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String forecast=mForecastAdapter.getItem(position);
                    Intent intent=new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,forecast);
                    startActivity(intent);

                }
            });

            return rootView;
        }


        public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

            private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

            private String getReadableDateString(long time){
                SimpleDateFormat shortenedDateFormat=new SimpleDateFormat("EEE MMM dd");
                return  shortenedDateFormat.format(time);
            }

            private  String formatHighLows(double high,double low){

                SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences((getActivity()));
                String unitType=sharedPreferences.getString(
                        getString(R.string.pref_unit_key),
                        getString(R.string.pref_unit_metric));
                if(unitType.equals(getString(R.string.pref_unit_imperial))){
                    high=(high*1.8)+32;
                    low=(low*1.8)+32;
                }else if (!unitType.equals(getString(R.string.pref_unit_metric))){
                    Log.d(LOG_TAG,"Unit type not found:"+unitType);
                }


                long roundedHigh=Math.round(high);
                long roundedLow=Math.round(low);
                String highLowStr=roundedHigh+"/"+roundedLow;
                return  highLowStr;
            }



            private String[] getWeatherDataFromJson(String forecastJsonStr,int numDays)
                    throws JSONException{
                final String OWM_LIST="list";
                final String OWM_WEATHER="weather";
                final String OWM_TEMPERATURE="temp";
                final String OWM_MAX="max";
                final String OWM_MIN="min";
                final String OWM_DESCRIPTION="main";

                JSONObject forecastJson=new JSONObject(forecastJsonStr);
                JSONArray weatherArray=forecastJson.getJSONArray(OWM_LIST);

                Time dayTime=new Time();
                dayTime.setToNow();

                int julianStartDay=Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);
                dayTime=new Time();

                String[] resultStrs=new String[numDays];
                for(int i=0;i<weatherArray.length();i++){
                    String day;
                    String description;
                    String highAndlow;

                    JSONObject dayForecast=weatherArray.getJSONObject(i);

                    long dateTime;
                    dateTime=dayTime.setJulianDay(julianStartDay+i);
                    day=getReadableDateString(dateTime);

                    JSONObject weatherObject=dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    description=weatherObject.getString(OWM_DESCRIPTION);

                    JSONObject temperatureObject=dayForecast.getJSONObject(OWM_TEMPERATURE);
                    double high=temperatureObject.getDouble(OWM_MAX);
                    double low=temperatureObject.getDouble(OWM_MIN);

                    highAndlow=formatHighLows(high,low);
                    resultStrs[i]=day+"-"+description+"-"+highAndlow;
                }
                for(String s:resultStrs){
                    Log.v(LOG_TAG,"Forecast entry:"+s);
                }
                return resultStrs;


            }



            @Override
            protected String[] doInBackground(String... params) {

                if(params.length==0){
                    return null;
                }

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String forecastJsonStr = null;

                String format="json";
                String units="metric";
                int numDays=7;
                String appid="fb12aed3abbef0489bd485f6f687037e";

                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily";
                    final String QUERY_PARAM="q";
                    final String FORMAT_PARAM="mode";
                    final String UNITS_PARAM="units";
                    final String DAYS_PARAM="cnt";
                    final String APPID_PARAM="appid";
                    Uri buildUri=Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM,params[0])
                            .appendQueryParameter(FORMAT_PARAM,format)
                            .appendQueryParameter(UNITS_PARAM,units)
                            .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                            .appendQueryParameter(APPID_PARAM,appid)
                            .build();
                    URL url=new URL(buildUri.toString());
                    Log.v(LOG_TAG,"Built URI"+buildUri.toString());


                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    forecastJsonStr = buffer.toString();
                    Log.v(LOG_TAG,"Forecast JSON Sring"+forecastJsonStr);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }
                try{
                    return getWeatherDataFromJson(forecastJsonStr,numDays);
                }catch (JSONException e){
                    Log.e(LOG_TAG,e.getMessage(),e);
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(String[] result){
                if(result!=null){
                    mForecastAdapter.clear();
                    for(String dayForecastStr:result){
                        mForecastAdapter.add(dayForecastStr);
                    }
                }
            }

        }
}

