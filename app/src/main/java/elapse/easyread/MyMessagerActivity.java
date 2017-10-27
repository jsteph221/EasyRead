package elapse.easyread;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static java.security.AccessController.getContext;


public class MyMessagerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messager);

    }
    private void getConversations(){
        String url = Config.API + EasyReadSingleton.getInstance(this).getUserId() +"/chat";
        JsonObjectRequest req = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Error contacting server",Toast.LENGTH_LONG).show();

                    }
                });
        EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
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

                JSONArray participants

                if(mConvUsername != null){
                }
                if(mBookAuthor != null){
                    mBookAuthor.setText("By : " + b.getAuthor());
                }
                if(mBookIsbn != null){
                    mBookIsbn.setText("ISBN : " + b.getIsbn());
                }
                if(mBookImg != null){
                    mBookImg.setImageUrl(b.getImageUrl(),EasyReadSingleton.getInstance(getContext()).getImageLoader());
                }
            }
            return v;
        }
    }
}
