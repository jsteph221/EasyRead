package elapse.easyread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyMessengerActivity extends AppCompatActivity {
    private ArrayList<JSONObject> conversations = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getConversations();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.bar_add_button).setVisible(false);
        menu.findItem(R.id.bar_search_preferences).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.bar_logout:
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file_key),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("logged_user").commit();
                Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void getConversations(){
        String url = Config.API +"chat/?userId="+ EasyReadSingleton.getInstance(this).getUserId();
        JsonObjectRequest req = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray conv = response.optJSONArray("conversations");
                        if(conv.length() == 0 ){
                            findViewById(R.id.my_messager_noConv).setVisibility(View.VISIBLE);
                        }else{
                            for(int i = 0;i< conv.length();i++){
                                conversations.add(conv.optJSONObject(i));
                            }
                            setupView();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse == null){
                            Toast.makeText(getApplicationContext(),R.string.network_error,Toast.LENGTH_LONG).show();
                        }else if (error.networkResponse.statusCode == 404){
                           findViewById(R.id.my_messager_noConv).setVisibility(View.VISIBLE);
                        }else{
                            Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                        }

                    }
                });
        EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }
    private void setupView(){
        ListView mListView =(ListView) findViewById(R.id.my_messager_conversations);
        mListView.setAdapter(new ConversationListAdapter(getApplicationContext(),R.id.list_conv_topic,conversations));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String convId = conversations.get(position).optString("_id");
                Intent i = new Intent(MyMessengerActivity.this,MessagingActivity.class);
                i.putExtra("conversation_id",convId);
                startActivity(i);
            }
        });
    }

    class ConversationListAdapter extends ArrayAdapter {
        private ArrayList<JSONObject> conversations;
        private Context ctx;

        public ConversationListAdapter(Context context, int textViewResourceId, ArrayList<JSONObject> items) {
            super(context, textViewResourceId, items);
            this.conversations = items;
            this.ctx = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_element_mymessager, null);
            }
            JSONObject conversation = conversations.get(position);
            if (conversation != null) {
                TextView mConvUsername = (TextView) v.findViewById(R.id.list_conv_username);
                TextView mConvTopic = (TextView) v.findViewById(R.id.list_conv_topic);
                TextView mConvMsg = (TextView) v.findViewById(R.id.list_conv_lastmessage);

                JSONArray participants = conversation.optJSONArray("participants");
                String myUserid = EasyReadSingleton.getInstance(getApplicationContext()).getUserId();


                if(mConvUsername != null){
                    if(participants.optString(0).equals(myUserid)){
                        mConvUsername.setText("User : "+ participants.optString(1));
                    }else{
                        mConvUsername.setText("User : "+ participants.optString(0));
                    }
                }
                if(mConvTopic != null){
                    mConvTopic.setText("Topic : " + conversation.optString("topic"));
                }
                if(mConvMsg != null){
                    mConvMsg.setText(conversation.optJSONArray("messages").optJSONObject(0).optString("data"));
                }
            }
            return v;
        }
    }
}
