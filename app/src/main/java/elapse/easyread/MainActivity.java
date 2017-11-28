package elapse.easyread;


import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SearchPreferencesDialog.NoticeDialogListener, SearchTermsDialog.NoticeSearchDialogListener{
    private static final String TAG = "Main Activity";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.bar_search_preferences:
                SearchPreferencesDialog dialog = new SearchPreferencesDialog();
                dialog.show(getFragmentManager(),"SearchPrefDialog");
                return true;
            case R.id.bar_search_terms:
                SearchTermsDialog searchTermsDialog = new SearchTermsDialog();
                searchTermsDialog.show(getFragmentManager(),"SearchTermDialog");
                return true;
            case R.id.bar_add_button:
                Intent i = new Intent(MainActivity.this, AddExchangeActivity.class);
                startActivity(i);
                return true;
            case R.id.bar_logout:
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file_key),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("logged_user").commit();
                Intent i2 = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i2);
                finish();
                return true;
            case R.id.bar_messager:
                Intent im = new Intent(MainActivity.this,MyMessengerActivity.class);
                startActivity(im);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private void requestManualLocation(){
        Toast.makeText(MainActivity.this,"Error your getting location. Please manually enter a location to search around here.",Toast.LENGTH_LONG).show();
        SearchPreferencesDialog dialog = new SearchPreferencesDialog();
        dialog.show(getFragmentManager(),"SearchPrefDialog");
    }

    @Override
    public void onPreferenceChange(DialogFragment dialog, boolean newLocation) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent i = new Intent("TAG_REFRESH");
        lbm.sendBroadcast(i);
    }

    @Override
    public void onSearch(DialogFragment dialog, String searchTerms, String sort) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent i = new Intent("TAG_RFRESH");
        i.putExtra("search_terms",searchTerms);
        i.putExtra("sort_by",sort);
        lbm.sendBroadcast(i);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentList.add(new ExchangesFragment());
            mFragmentTitleList.add("All Exchanges");
            mFragmentList.add(new MyExchangesFragment());
            mFragmentTitleList.add("My Exchanges");
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
