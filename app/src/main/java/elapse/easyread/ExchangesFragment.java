package elapse.easyread;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Joshua on 2017-10-07.
 */



public class ExchangesFragment extends Fragment {
    private ArrayList<BookExchange> exchanges;
    private int maxDistance = 25000; //Make choice;
    public ExchangesFragment(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        getExchanges();

        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.exchange_fragment, container, false);
    }

    private void getExchanges(){
        String reqUrl = Config.API + "exchanges";
        reqUrl += "/?longitude="+longitude+"&latitude="+latitude+"maxDistance="+maxDistance;
        JsonObjectRequest req = new JsonObjectRequest
                (Request.Method.GET, reqUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("200")  ){
                                exchanges = parseJsonResponse(response.getJSONObject("exchanges"));
                                setupListView();
                            }else{
                                Toast.makeText(getContext(),"No books found. Increase distance or change parameters",Toast.LENGTH_LONG);
                                Log.d(TAG,response.getString("message"));
                            }


                        }catch(JSONException e){
                            Log.d(TAG,e.getMessage());

                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),"Error contacting server",Toast.LENGTH_LONG).show();

                    }
                });
        EasyReadSingleton.getInstance(this.getContext()).addToRequestQueue(req);
    }

    private void setupListView(){
        ListView listView = (ListView) getView().findViewById(R.id.exchanges_list);
        ListViewAdapter adapter = new ListViewAdapter(this.getContext(),R.id.list_book_title,exchanges);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("Exhanges","Exchange Clicked");
                //Intent i = new Intent(this,ShowExchange.class);
            }
        });
    }

    private ArrayList<BookExchange> parseJsonResponse(JSONObject response){
        ArrayList<BookExchange> exchanges = new ArrayList<>();
        //TODO: Parse response into array of BookExchanges
        return exchanges;
    }


}
