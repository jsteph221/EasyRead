package elapse.easyread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;


public class ShowExchangeActivity extends AppCompatActivity {
    private BookExchange exchange;

    private CustomNetworkImageView mImageView;
    private TextView mTitleView;
    private TextView mAuthorView;
    private TextView mPosterView;
    private TextView mDescriptionView;
    private TextView mIsbnView;
    private TextView mLocationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_exchange);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        exchange = getIntent().getParcelableExtra("book_exchange");
        mImageView = (CustomNetworkImageView) findViewById(R.id.show_image_view);
        mTitleView = (TextView) findViewById(R.id.show_ex_title);
        mAuthorView=(TextView) findViewById(R.id.show_ex_author);
        mPosterView=(TextView) findViewById(R.id.show_ex_poster);
        mDescriptionView = (TextView) findViewById(R.id.show_description);
        mIsbnView = (TextView) findViewById(R.id.show_isbn);
        mLocationView = (TextView) findViewById(R.id.show_location);


        mImageView.setImageUrl(exchange.getBook().getImageUrl(),EasyReadSingleton.getInstance(getApplicationContext()).getImageLoader());
        mTitleView.setText(exchange.getBook().getTitle());
        mAuthorView.setText(exchange.getBook().getAuthor());
        mPosterView.setText("Posted By :"+exchange.getPosterId());
        mDescriptionView.setText(exchange.getDescription());
        if(exchange.getBook().getIsbn().equals("")){
            mIsbnView.setText("ISBN : N/A");
        }else{
            mIsbnView.setText("ISBN : " + exchange.getBook().getIsbn());
        }
        mLocationView.setText(exchange.getLocationName());
        Button mMessageButton = (Button) findViewById(R.id.show_message);
        if(exchange.getPosterId().equals(EasyReadSingleton.getInstance(getApplicationContext()).getUserId())){
            mMessageButton.setVisibility(View.GONE);
        }else{
            mMessageButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ShowExchangeActivity.this,MessagingActivity.class);
                    i.putExtra("to_user",exchange.getPosterId());
                    i.putExtra("book_title",exchange.getBook().getTitle());
                    startActivity(i);
                }
            });
        }

    }
    private void showProgress(boolean bool){
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressSpinner);
        bar.setVisibility(bool ? View.VISIBLE : View.GONE);
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
                Intent im = new Intent(ShowExchangeActivity.this,MyMessengerActivity.class);
                startActivity(im);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
