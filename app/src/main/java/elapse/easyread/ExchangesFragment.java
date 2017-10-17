package elapse.easyread;

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

import org.json.JSONArray;
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

    public static ExchangesFragment newInstance(int sectionNum){
        ExchangesFragment fragment = new ExchangesFragment();
        Bundle args = new Bundle();
        args.putInt("section_number",sectionNum);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_exchange_fragment, container, false);
        getExchanges();
        // Inflate the layout for this fragment
        return rootView;
    }

    private void getExchanges(){
        String reqUrl = Config.API + "exchanges";
        String longitude = "49.3";
        String latitude = "-122.84";
        reqUrl += "/?longitude="+longitude+"&latitude="+latitude+"&maxDistance="+maxDistance;
        JsonObjectRequest req = new JsonObjectRequest
                (Request.Method.GET, reqUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        exchanges = parseJsonResponse(response);
                        setupListView();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),R.string.api_error,Toast.LENGTH_LONG).show();

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
        ArrayList<BookExchange> exchs = new ArrayList<>();
        try{
            JSONArray exchResponse = response.getJSONArray("exchanges");
            for (int i = 0;i < exchResponse.length();i++){
                JSONObject exch = exchResponse.getJSONObject(i);
                exchs.add(new BookExchange(exch));
            }

        }catch (JSONException e){

        }
        return exchs;
    }


}
