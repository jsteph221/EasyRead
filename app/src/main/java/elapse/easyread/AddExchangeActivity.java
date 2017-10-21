package elapse.easyread;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.android.volley.Request.Method.POST;


public class AddExchangeActivity extends AppCompatActivity implements PickBookDialog.NoticeDialogListener {
    private static final String TAG = "ADD_EXCHANGE_ACTIVITY";

    private EditText mSearchInputView;
    private EditText mTitleView;
    private EditText mAuthorView;
    private NetworkImageView mImageView;
    private EditText mDescriptionView;
    private ProgressBar mProgressBar;
    private EditText mIsbnView;
    PlaceAutocompleteFragment mAutocompleteFragment;

    private String currentImgURL;
    private Place chosenPlace;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_exhange);
        mSearchInputView = (EditText) findViewById(R.id.search_input);
        mTitleView = (EditText) findViewById(R.id.add_exchange_title);
        mAuthorView = (EditText) findViewById(R.id.add_exchange_author);
        mImageView = (NetworkImageView) findViewById(R.id.add_exchange_image);
        mDescriptionView = (EditText) findViewById(R.id.add_exchange_description);
        mProgressBar = (ProgressBar) findViewById(R.id.add_exchange_progress);
        mIsbnView = (EditText) findViewById(R.id.add_isbn_entry);
        mAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.pref_dialog_place_auto);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        EditText desc = (EditText) findViewById(R.id.add_exchange_description);
        desc.setMovementMethod(new ScrollingMovementMethod());
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                chosenPlace = place;
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(),"Error getting place data",Toast.LENGTH_LONG).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        setupButtons();


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

    @Override
    public void onBookClick(DialogFragment dialog,Book clickedBook) {
        currentImgURL = clickedBook.getImageUrl();
        mImageView.setImageUrl(currentImgURL,EasyReadSingleton.getInstance(getApplicationContext()).getImageLoader());
        mTitleView.setText(clickedBook.getTitle());
        mAuthorView.setText(clickedBook.getAuthor());
        mIsbnView.setText(clickedBook.getIsbn());

    }


    private void setupButtons(){
        ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchInputView.setError(null);
                String query = mSearchInputView.getText().toString().replaceAll(" ","+");
                if (TextUtils.isEmpty(query)){
                    mSearchInputView.setError("Input Required to Search");
                    mSearchInputView.requestFocus();
                }else{
                    showProgress(true);
                    JsonObjectRequest req = new JsonObjectRequest
                            (Request.Method.GET, Config.GOOGLEBOOKSAPI + query, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try{
                                        if (!response.getString("totalItems").equals("0")){
                                            ArrayList<Book> books =jsonRespToBookArray(response.getJSONArray("items"));

                                            PickBookDialog dialog = new PickBookDialog();
                                            showProgress(false);
                                            Bundle args = new Bundle();
                                            args.putParcelableArrayList("foundBooks",books);
                                            dialog.setArguments(args);
                                            dialog.show(getFragmentManager(),"Picking Book");
                                        }else{
                                            Toast.makeText(getApplicationContext(),"No books found for given search parameters",Toast.LENGTH_SHORT).show();
                                        }

                                    }catch(JSONException e){
                                        Log.d(TAG,e.getMessage());

                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),"Error Searching",Toast.LENGTH_SHORT).show();
                                }
                            });
                    EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
                }
            }
        });
        ImageButton scanButton = (ImageButton) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG,"Onclick scan");
            }
        });

        Button addButton = (Button) findViewById(R.id.add_exchange_button);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean cancel = false;
                View focusView = null;

                String title = mTitleView.getText().toString();
                String author = mAuthorView.getText().toString();
                String imgUrl = currentImgURL;
                String description = mDescriptionView.getText().toString();
                String isbn = mIsbnView.getText().toString();
                Place place = chosenPlace;
                if(TextUtils.isEmpty(title)){
                    mTitleView.setError(getString(R.string.add_exch_required_field));
                    cancel = true;
                    focusView = mTitleView;
                }else if(TextUtils.isEmpty(author)){
                    mTitleView.setError(getString(R.string.add_exch_required_field));
                    cancel = true;
                    focusView = mTitleView;
                }
                else if (chosenPlace == null){
                    Toast.makeText(getApplicationContext(),"Location required to add exchange",Toast.LENGTH_SHORT).show();
                    cancel = true;
                }
                else if(!TextUtils.isEmpty(isbn) && !isValidIsbn(isbn)){
                    mIsbnView.setError("Invalid Isbn. Delete completely or enter valid isbn (no spaces)");
                    cancel = true;
                    focusView = mIsbnView;
                }
                if (cancel){
                    if(focusView !=null){
                        focusView.requestFocus();
                    }
                }else{
                    showProgress(true);
                    Book b = new Book(title,author,imgUrl,isbn);
                    if(TextUtils.isEmpty(description)){
                        description = "";
                    }
                    if (TextUtils.isEmpty(isbn)){
                        isbn = "";
                    }
                    BookExchange newExchange = new BookExchange(b,EasyReadSingleton.getInstance(getApplicationContext()).getUserId(),
                                            place.getName().toString(),place.getLatLng(),description);
                    sendExchangeToServer(newExchange);

                }

        }});
    }

    private void sendExchangeToServer(BookExchange ex){
        String reqUrl = Config.API+"users/"+EasyReadSingleton.getInstance(getApplicationContext()).getUserId()+"/exchanges";
        JSONObject body= ex.toJsonBookExchange();
        JsonObjectRequest req = new JsonObjectRequest(POST, reqUrl,body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Added Exchange",Toast.LENGTH_LONG).show();
                resetViews();
                showProgress(false);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,error.getMessage());
                Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        });
        EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void resetViews(){
        mSearchInputView.setText("");
        mTitleView.setText("");
        mAuthorView.setText("");
        mImageView.setImageDrawable(null);
        mDescriptionView.setText("");
        mIsbnView.setText("");
    }

    private ArrayList<Book> jsonRespToBookArray(JSONArray items){
        ArrayList<Book> books = new ArrayList<>();
        for (int i = 0; i< items.length();i++){
            try{
                Book b = jsonBookToBook(items.getJSONObject(i));
                if (b != null){
                    books.add(b);
                }
            }catch(JSONException e){
                Log.d(TAG,e.toString());
            }

        }
        return books;

    }

    private Book jsonBookToBook(JSONObject info){
        try{
            //String id = info.getString("id");
            JSONObject volInfo = info.getJSONObject("volumeInfo");
            String title = volInfo.getString("title");
            String author = volInfo.getJSONArray("authors").getString(0);
            String isbn10 = volInfo.getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier");
            String imgUrl = volInfo.getJSONObject("imageLinks").getString("smallThumbnail");
            return new Book(title,author,imgUrl,isbn10);


        }catch (JSONException e){

        }
        return null;
    }
    private boolean isValidIsbn(String isbn){
        //TODO
        return true;
        /*
        int sum = 0 ;
        if(isbn.length() == 10){
            String revIsbn = new StringBuilder(isbn).reverse().toString();
            for (int i = 0; i<10;i++){
                sum += i * Character.getNumericValue(revIsbn.charAt(i));
            }
            return (sum % 11) == 0;
        } else if (isbn.length() == 13){
            for(int i = 0;i < 10;i++){
                if (i%2 == 0){
                    sum+= Character.getNumericValue(isbn.charAt(i));
                }else{
                    sum+= Character.getNumericValue(isbn.charAt(i))*3;
                }
            }
            return (10 - (sum %10)) ==
            if ( Character.getNumericValue(isbn.charAt(12)) == 10 - (sum%10)) return true;
        }
        return false;
        */
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

}
