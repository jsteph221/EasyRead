package elapse.easyread;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Fragment of MainActivity. Contacts server for nearby exchanges.
 */



public class ExchangesFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "ExchangesFragment";
    private ArrayList<BookExchange> exchanges;
    private LatLng CURRENT_LOCATION;

    private static final int  MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ListViewAdapter mAdapter;
    private ListView mListView;

    //Recives broadcasts from mainactivity when user changes maxDistance or manual location
    MyReceiver r;

    public ExchangesFragment(){}





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search_exchanges, container, false);
        exchanges = new ArrayList<>();
        mListView = (ListView) rootView.findViewById(R.id.exchanges_list);
        mAdapter = new ListViewAdapter(this.getContext(),R.id.list_book_title,exchanges);
        mListView.setAdapter(mAdapter);
        getExchanges();
        return rootView;
    }

    //Runs when user responds to permission request.
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull
                                           String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupLocationServices();

                } else {
                    setupLocationError();
                }
            }
        }
    }

    /*
        Check if location permitted, if yes get last location, else request permission.
     */
    private void setupLocationServices(){
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            Task t = mFusedLocationClient.getLastLocation();
            //Async get last location. If failure or null, show location error
            t.addOnSuccessListener(getActivity(), new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    if (o == null){
                        setupLocationError();
                    }else{
                        Location loc = (Location) o;
                        CURRENT_LOCATION = new LatLng(loc.getLatitude(),loc.getLongitude());
                        EasyReadSingleton.getInstance(getContext()).setSearchLocationLatLng(CURRENT_LOCATION);
                        getExchanges();
                    }
                }
            });
            t.addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    setupLocationError();
                }
            });
        }
    }

    /*
        Get Current location from singleton. If exists get exchanges from server.
        else call setuplocationservices.
     */
    public void getExchanges(){
        CURRENT_LOCATION = EasyReadSingleton.getInstance(getContext()).getSearchLocationLatLng();
        if(CURRENT_LOCATION != null){
            String reqUrl = Config.API + "exchanges";
            EasyReadSingleton ins = EasyReadSingleton.getInstance(getContext());
            String longitude = Double.toString(CURRENT_LOCATION.longitude);
            String latitude = Double.toString(CURRENT_LOCATION.latitude);
            String maxDistance = ins.getSearchMaxDistance();
            reqUrl += "/?longitude="+longitude+"&latitude="+latitude+"&maxDistance="+maxDistance+"&username="+EasyReadSingleton.getInstance(getActivity()).getUserId();
            JsonObjectRequest req = new JsonObjectRequest
                    (Request.Method.GET, reqUrl, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            parseJsonResponse(response);
                            setupListView();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(),R.string.api_error,Toast.LENGTH_LONG).show();

                        }
                    });
            EasyReadSingleton.getInstance(this.getContext()).addToRequestQueue(req);
        }else{
            setupLocationServices();
        }
    }
    //Display exchanges
    private void setupListView(){
        mAdapter.notifyDataSetChanged();
        mListView.setVisibility(View.VISIBLE);
    }
    //Populate exchanges with BookExchanges from server response
    private void parseJsonResponse(JSONObject response){
        exchanges.clear();
        try{
            JSONArray exchResponse = response.getJSONArray("exchanges");
            for (int i = 0;i < exchResponse.length();i++){
                JSONObject exch = exchResponse.getJSONObject(i);
                exchanges.add(new BookExchange(exch));
            }

        }catch (JSONException e){
            Log.d(TAG,"Error parsing bookexchanges from server");
        }
    }

    //On failure to connect to googleplayservices
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        setupLocationError();
    }
    //Display textview explaining location error. Request manual.
    private void setupLocationError(){
        TextView mLocationErrorText = (TextView) getView().findViewById(R.id.location_error_text);
        mLocationErrorText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }
    @Override
    public void onResume() {
        super.onResume();
        r = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));
    }

    //Class to recieve broadcasts. On recieve, refresh exchanges and search again
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ExchangesFragment.this.getExchanges();
        }
    }


}
