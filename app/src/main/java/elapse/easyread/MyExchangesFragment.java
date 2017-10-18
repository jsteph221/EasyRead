package elapse.easyread;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Joshua on 10/14/2017.
 */

public class MyExchangesFragment extends Fragment {
    private ArrayList<BookExchange> exchanges;
    private View mView;

    private boolean refreshOnResume= false;
    public MyExchangesFragment(){}

    public static MyExchangesFragment newInstance(int sectionNum){
        MyExchangesFragment fragment = new MyExchangesFragment();
        Bundle args = new Bundle();
        args.putInt("section_number",sectionNum);
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my_exchanges, container, false);
        getExchanges();
        return mView;
    }


    private void getExchanges(){
        showProgress(true);

        String reqUrl = Config.API + "users/"+EasyReadSingleton.getInstance(getContext()).getUserId()+"/exchanges";
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
                        Toast.makeText(getContext(),"Error contacting server",Toast.LENGTH_LONG).show();

                    }
                });
        EasyReadSingleton.getInstance(this.getContext()).addToRequestQueue(req);
    }

    private void setupListView() {
        ListView listView = (ListView) mView.findViewById(R.id.my_exchanges_list);
        ListViewAdapter adapter = new ListViewAdapter(this.getContext(), R.id.list_book_title, exchanges);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("Exhanges", "Exchange Clicked");
                //TODO:Intent i = new Intent(this,ShowExchange.class);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            //TODO
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Choose Action").setItems(R.array.dialog_choose_action_myexchanges, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                //sendRemoveExchangeToServer(
                                Log.d("Dialog","delete exchange");
                                break;
                            case 1:
                                //edit
                                break;
                            default:
                                break;
                        }

                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface d, int arg1) {
                        d.cancel();
                    };
                });
                builder.show();
                return true;
            }
        });
        showProgress(false);
    }
    private void showProgress(boolean bool){
        ProgressBar bar = (ProgressBar) mView.findViewById(R.id.my_exchange_progress);
        bar.setVisibility(bool ? View.VISIBLE : View.GONE);
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
    @Override
    public void onResume() {
        super.onResume();
        // Check should we need to refresh the fragment
        if(refreshOnResume){
            getExchanges();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshOnResume = true;
    }
}

