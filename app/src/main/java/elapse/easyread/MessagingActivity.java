package elapse.easyread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.android.volley.Request.Method.POST;

public class MessagingActivity extends AppCompatActivity {
    private static final String TAG = "Messaging Activity";
    private ArrayList<Message> messages;
    private String topic;
    private String otherUser;

    private boolean newConversation;
    private String conversationId;
    private int pageNum = 0;
    private String myUserId;

    private EditText mToView;
    private EditText mTopicView;
    private EditText mDataView;
    private MessagesListAdapter mListViewAdapter;

    private LocalBroadcastManager bManager;
    private BroadcastReceiver bReciever;
    private static final String RECIEVE_MESSAGE = "RECIEVED_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(RECIEVE_MESSAGE)){
                    Log.d(TAG,"Recieved Message");
                    Message msg = new Message(intent.getStringExtra("from_user"),intent.getStringExtra("data"));
                    messages.add(msg);
                    mListViewAdapter.notifyDataSetChanged();
                }

            }
        };
        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECIEVE_MESSAGE);
        bManager.registerReceiver(bReciever,intentFilter);

        mToView = (EditText) findViewById(R.id.messaging_to);
        mTopicView = (EditText) findViewById(R.id.messaging_topic);
        mDataView = (EditText) findViewById(R.id.messaging_data);
        conversationId = getIntent().getStringExtra("conversation_id");
        myUserId = EasyReadSingleton.getInstance(getApplicationContext()).getUserId();
        showProgress(true);
        if(conversationId == null){
            messages = new ArrayList<>();
            otherUser = getIntent().getStringExtra("to_user");
            topic = getIntent().getStringExtra("book_title");
            newConversation = true;
            setupView();
        }else{
            newConversation = false;
            getMessagesFromServer(conversationId);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.bar_add_button).setVisible(false);
        menu.findItem(R.id.bar_search_preferences).setVisible(false);
        menu.findItem(R.id.search_terms).setVisible(false);

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
    @Override
    protected void onDestroy(){
        super.onDestroy();
        bManager.unregisterReceiver(bReciever);
    }
    private void getMessagesFromServer(String id){
        String reqUrl = Config.API + "chat/"+id+"/?pageNum="+pageNum;

        JsonObjectRequest req = new JsonObjectRequest
                (Request.Method.GET, reqUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        messages = parseJsonResponse(response.optJSONArray("messages"));
                        topic = response.optString("topic");
                        JSONArray participants = response.optJSONArray("participants");
                        if(participants.optString(0).equals(myUserId)){
                            otherUser = participants.optString(1);
                        }else{
                            otherUser = participants.optString(0);
                        }
                        setupView();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Error contacting server",Toast.LENGTH_LONG).show();
                        showProgress(false);
                        onBackPressed();

                    }
                });
        EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }
     private void setupView(){
         if(otherUser != null){
             mToView.setText(otherUser);
             mToView.setEnabled(false);
         }
         if(topic != null){
             mTopicView.setText(topic);
             mTopicView.setEnabled(false);
         }
         ListView mListView = (ListView) findViewById(R.id.messaging_messages);
         mListViewAdapter = new MessagesListAdapter(getApplicationContext(),R.id.message_data_from,messages);
         mListView.setAdapter(mListViewAdapter);
         showProgress(false);
         //Buttons
         Button mSendButton = (Button) findViewById(R.id.messaging_send_button);
         mSendButton.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 boolean cancel = false;
                 View focusView = null;

                 String userTo = mToView.getText().toString();
                 String topic = mTopicView.getText().toString();
                 final String message = mDataView.getText().toString();

                 if(TextUtils.isEmpty(userTo)){
                     mToView.setError(getString(R.string.required_field));
                     focusView = mToView;
                     cancel = true;
                 }
                 if(TextUtils.isEmpty(topic)){
                     mTopicView.setError(getString(R.string.required_field));
                     focusView = mTopicView;
                     cancel = true;
                 }
                 if(TextUtils.isEmpty(message)){
                     mDataView.setError(getString(R.string.required_field));
                     focusView = mDataView;
                     cancel = true;
                 }
                 if(cancel){
                     focusView.requestFocus();
                 }else {
                     String url;
                     JSONObject body = new JSONObject();
                     if(newConversation){
                         url = Config.API +"chat";
                         ArrayList<String> participants = new ArrayList<>();
                         participants.add(myUserId);
                         participants.add(userTo);
                         try{
                             body.put("participants",new JSONArray(participants));
                             body.put("topic",topic);
                             JSONObject msg = new JSONObject();
                             msg.put("sentBy", myUserId);
                             msg.put("data", message);
                             body.put("messages", msg);
                         }catch (JSONException e){
                             Log.d("Messaging Send ERROR",e.getMessage());

                         }
                     }else{
                         url =  Config.API + "chat/"+conversationId;
                         try{
                             body.put("sentBy",myUserId);
                             body.put("message", message);
                         }catch (JSONException e){
                             Log.d("Messaging Send ERROR",e.getMessage());
                         }
                     }
                     JsonObjectRequest req = new JsonObjectRequest(POST, url,body, new Response.Listener<JSONObject>() {
                         @Override
                         public void onResponse(JSONObject response) {
                             messages.add(new Message(myUserId,message));
                             mListViewAdapter.notifyDataSetChanged();
                             newConversation = false;
                             showProgress(false);
                         }
                     }, new Response.ErrorListener() {

                         @Override
                         public void onErrorResponse(VolleyError error) {
                             Log.d("Messaging Send ERROR",error.toString());
                             Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                             showProgress(false);
                         }
                     });
                     EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
                 }
             }
         });
     }
    private ArrayList<Message> parseJsonResponse(JSONArray response){
        ArrayList<Message> messages= new ArrayList<>();
        for(int i = 0; i<response.length();i++){
            messages.add(new Message(response.optJSONObject(i)));
        }
        return messages;
    }

    private void showProgress(boolean bool){
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressSpinner);
        bar.setVisibility(bool ? View.VISIBLE : View.GONE);
    }
    class Message{
        protected String sentBy;
        protected String data;

        public Message(String sentBy,String data){
            this.sentBy = sentBy;
            this.data = data;
        }
        public Message(JSONObject jsonMessage){
            this.sentBy = jsonMessage.optString("sentBy");
            this.data = jsonMessage.optString("data");
        }
    }
    class MessagesListAdapter extends ArrayAdapter {
        private ArrayList<Message> messages;
        private Context ctx;

        public MessagesListAdapter(Context context, int textViewResourceId, ArrayList<Message> items) {
            super(context, textViewResourceId, items);
            this.messages = items;
            this.ctx = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_element_messaging, null);
            }
            Message m = messages.get(position);
            String myUserid = EasyReadSingleton.getInstance(getApplicationContext()).getUserId();
            if (m != null) {
                TextView mMsgView;
                if(m.sentBy.equals(myUserid)){
                    mMsgView = (TextView) v.findViewById(R.id.message_data_from);
                }else{
                    mMsgView = (TextView) v.findViewById(R.id.message_data_to);
                }
                mMsgView.setText(m.data);
                mMsgView.setVisibility(View.VISIBLE);
            }
            return v;
        }
    }


}
