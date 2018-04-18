package com.quicklift;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by adarsh on 15/2/18.
 */

public class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyCipfj3faTVrYbN30RZWyFSaOb6TE593Sw";
    private ArrayList<String> resultList;
    private ArrayList<String> resultkeyList=new ArrayList<>();
    private Context context = null;
    public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    @Override
    public int getCount() {
        if(resultList != null)
            return resultList.size();
        else
            return 0;
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    public ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;
        ArrayList<String> descriptionList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:in");
            sb.append("&location=12.972442,77.580643&radius=30000");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            //Log.v("TAG",  sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            //Log.v("TAG",  e.toString());
            return resultList;
        } catch (IOException e) {
            //Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            //Log.d("yo",jsonResults.toString());
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            descriptionList = new ArrayList(predsJsonArray.length());
            //resultkeyList = new ArrayList(predsJsonArray.length());
            resultkeyList.clear();
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).toString());
                descriptionList.add(predsJsonArray.getJSONObject(i).getString("description"));
                resultkeyList.add(predsJsonArray.getJSONObject(i).getString("place_id"));
            }

            //saveArray(resultList.toArray(new String[resultList.size()]), "predictionsArray", getContext());
        } catch (JSONException e) {
            //Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        //Log.v("TAG",  String.valueOf(descriptionList.size()));
        return descriptionList;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                   // setImageVisibility();
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    public ArrayList<String> getkeys(){
        return resultkeyList;
    }
}