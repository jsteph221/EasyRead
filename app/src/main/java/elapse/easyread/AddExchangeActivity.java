package elapse.easyread;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.R.attr.bitmap;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
/*
    Activity for user adding a book exchange
 */

public class AddExchangeActivity extends AppCompatActivity implements PickBookDialog.NoticeDialogListener {
    private static final String TAG = "ADD_EXCHANGE_ACTIVITY";
    private static final int IMAGE_REQUEST_CODE = 1;

    //View variables
    private EditText mSearchInputView;
    private EditText mTitleView;
    private EditText mAuthorView;
    private CustomNetworkImageView mImageView;
    private EditText mDescriptionView;
    private ProgressBar mProgressBar;
    private EditText mIsbnView;
    PlaceAutocompleteFragment mAutocompleteFragment;
    private ImageButton mSearchButton;
    private ImageButton mScanButton;



    //State
    private String currentImgURL;
    private LatLng chosenLoc;
    private String chosenLocName;
    private boolean edit;
    private BookExchange exchangeToEdit;
    private Bitmap customImgBitmap;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_exchange);
        mSearchInputView = (EditText) findViewById(R.id.search_input);
        mTitleView = (EditText) findViewById(R.id.add_exchange_title);
        mAuthorView = (EditText) findViewById(R.id.add_exchange_author);
        mImageView = (CustomNetworkImageView) findViewById(R.id.add_exchange_image);
        mDescriptionView = (EditText) findViewById(R.id.add_exchange_description);
        mProgressBar = (ProgressBar) findViewById(R.id.progressSpinner);
        mIsbnView = (EditText) findViewById(R.id.add_isbn_entry);
        mAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.pref_dialog_place_auto);
        mSearchButton = (ImageButton) findViewById(R.id.search_button);
        mScanButton = (ImageButton) findViewById(R.id.scan_button);
        LatLng loc = EasyReadSingleton.getInstance(getApplicationContext()).getSearchLocationLatLng();
        if(loc != null){
            mAutocompleteFragment.setText("Use current location: ("+loc.latitude+","+loc.longitude);
            chosenLoc = loc;
            chosenLocName = loc.latitude + ","+loc.longitude;
        }

        //Check If this is adding an Exchange or Editing a preexisting one.
        edit = getIntent().getBooleanExtra("edit",false);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if(edit){
                toolbar.setTitle("Edit Exchange");
            }
        }

        mDescriptionView.setMovementMethod(new ScrollingMovementMethod());
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                chosenLoc = place.getLatLng();
                chosenLocName = place.getName().toString();
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(),"Error getting place data",Toast.LENGTH_LONG).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        setupButtons();
        if(edit){
            TextView searchScanTitle = (TextView) findViewById(R.id.search_scan_title);
            searchScanTitle.setVisibility(View.GONE);
            mSearchInputView.setVisibility(View.GONE);
            mSearchButton.setVisibility(View.GONE);
            mScanButton.setVisibility(View.GONE);
            exchangeToEdit = getIntent().getParcelableExtra("exchange");
            currentImgURL = exchangeToEdit.getBook().getImageUrl();
            populateViews(exchangeToEdit);
        }


    }

    private void populateViews(BookExchange ex){
        mImageView.setImageUrl(ex.getBook().getImageUrl(),EasyReadSingleton.getInstance(this).getImageLoader());
        mTitleView.setText(ex.getBook().getTitle());
        mAuthorView.setText(ex.getBook().getAuthor());
        if(ex.getBook().getIsbn() != null){
            mIsbnView.setText(ex.getBook().getIsbn());
        }
        chosenLocName = ex.getLocationName();
        mAutocompleteFragment.setText(ex.getLocationName());
        chosenLocName = ex.getLocationName();
        chosenLoc = ex.getLatLng();
        mDescriptionView.setText(ex.getDescription());
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
                editor.remove("logged_user").apply();
                Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.bar_messager:
                Intent im = new Intent(AddExchangeActivity.this,MyMessengerActivity.class);
                startActivity(im);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //Function for dialog listener.Setup views with clicked book
    @Override
    public void onBookClick(DialogFragment dialog,Book clickedBook) {
        currentImgURL = clickedBook.getImageUrl();
        mImageView.setImageUrl(currentImgURL,EasyReadSingleton.getInstance(getApplicationContext()).getImageLoader());
        mTitleView.setText(clickedBook.getTitle());
        mTitleView.setText(clickedBook.getTitle());
        mAuthorView.setText(clickedBook.getAuthor());
        mIsbnView.setText(clickedBook.getIsbn());
        //Since bookimage chosed from google, make custom null.
        customImgBitmap = null;
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
        if(edit){
            addButton.setText("Edit Exchange");
        }
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean cancel = false;
                View focusView = null;

                String title = mTitleView.getText().toString();
                String author = mAuthorView.getText().toString();
                String description = mDescriptionView.getText().toString();
                String isbn = mIsbnView.getText().toString();
                if(TextUtils.isEmpty(title)){
                    mTitleView.setError(getString(R.string.required_field));
                    cancel = true;
                    focusView = mTitleView;
                }else if(TextUtils.isEmpty(author)){
                    mTitleView.setError(getString(R.string.required_field));
                    cancel = true;
                    focusView = mTitleView;
                }
                else if (chosenLoc == null){
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
                    if(TextUtils.isEmpty(description)){
                        description = "";
                    }
                    if (TextUtils.isEmpty(isbn)){
                        isbn = "";
                    }
                    //If image from google, create book with image
                    if(currentImgURL != null){
                        Book b = new Book(title,author,currentImgURL,isbn);
                        BookExchange newExchange = new BookExchange(b,EasyReadSingleton.getInstance(getApplicationContext()).getUserId(),
                                chosenLocName,chosenLoc,description);
                        if(edit){
                            sendEditToServer(newExchange);
                        }else{
                            sendExchangeToServer(newExchange);
                        }
                    }else{
                        //Convert bitmap to base64 and create book.
                        //Store image on backend, on callback store theexchange with the new url
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        Book b = new Book(title,author,"",isbn);
                        final BookExchange newExchange = new BookExchange(b,EasyReadSingleton.getInstance(getApplicationContext()).getUserId(),
                                chosenLocName,chosenLoc,description);
                        customImgBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                        String image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        JSONObject body = new JSONObject();
                        try{
                            body.put("base64Image",image);
                        }catch (JSONException e){}
                        String reqUrl = Config.API+"image";

                        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,reqUrl, body,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        newExchange.getBook().setImageUrl(Config.API+"image/"+response.optString("imageId"));
                                        if(edit){
                                            sendEditToServer(newExchange);
                                        }else{
                                            sendExchangeToServer(newExchange);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("VOLLEY", error.toString());
                                        Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                                        showProgress(false);
                                    }
                                });
                        EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
                    }
                }

        }});
        Button mUploadImage = (Button) findViewById(R.id.add_exchange_upload);
        mUploadImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            currentImgURL = null;
            customImgBitmap = (Bitmap) data.getExtras().get("data");
            mImageView.setImageBitmap(customImgBitmap);
        }

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
                Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        });
        EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void sendEditToServer(BookExchange ex){
        String reqUrl = Config.API+"exchanges/"+exchangeToEdit.getId();
        if(customImgBitmap != null){
            reqUrl += "/?customImage=true";
        } 
        JSONObject body = ex.toJsonBookExchange();
        JsonObjectRequest req = new JsonObjectRequest(PUT,reqUrl,body,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Exchange Edited",Toast.LENGTH_LONG).show();
                resetViews();
                showProgress(false);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,error.getMessage());
                if(error.networkResponse == null){
                    Toast.makeText(getApplicationContext(),R.string.network_error,Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                }
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
            Log.d(TAG,"Error parsing json response to book");
            return null;
        }
    }
    private boolean isValidIsbn(String isbn){
        return (isbn.length()==13 || isbn.length()==10);
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


    private void showProgress(final boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mIsbnView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
        mAuthorView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
        mTitleView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
        mDescriptionView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
        mImageView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
        mSearchInputView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
    }

}
