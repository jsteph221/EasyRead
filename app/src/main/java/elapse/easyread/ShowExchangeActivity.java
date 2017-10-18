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
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public class ShowExchangeActivity extends AppCompatActivity {
    private BookExchange exchange;

    private NetworkImageView mImageView;
    private TextView mTitleView;
    private TextView mAuthorView;
    private TextView mPosterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_exchange);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        exchange = getIntent().getParcelableExtra("book_exchange");
        mImageView = (NetworkImageView) findViewById(R.id.show_image_view);
        mTitleView = (TextView) findViewById(R.id.show_ex_title);
        mAuthorView=(TextView) findViewById(R.id.show_ex_author);
        mPosterView=(TextView) findViewById(R.id.show_ex_poster);

        mImageView.setImageUrl(exchange.getBook().getImageUrl(),EasyReadSingleton.getInstance(getApplicationContext()).getImageLoader());
        mTitleView.setText(exchange.getBook().getTitle());
        mAuthorView.setText(exchange.getBook().getAuthor());
        mPosterView.setText("Posted By :"+exchange.getPosterId());
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

}
