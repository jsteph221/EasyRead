package elapse.easyread;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Joshua on 10/17/2017.
 */

public class SearchPreferencesDialog extends DialogFragment {

    private String chosenPlaceName;
    private LatLng chosenPlaceLatLng;
    private PlaceAutocompleteFragment mAutoCompleteFragment;

    NoticeDialogListener mListener;

    public interface NoticeDialogListener {
        public void onPreferenceChange(DialogFragment dialog,boolean newLocation);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_preferences,null);

        final TextView mMaxRadiusDisplay = (TextView) v.findViewById(R.id.max_distance_display);
        String maxDistance = EasyReadSingleton.getInstance().getSearchMaxDistance();
        mMaxRadiusDisplay.setText(maxDistance + " km");

        final SeekBar mMaxRadiusBar = (SeekBar) v.findViewById(R.id.max_distance_seekbar);
        mMaxRadiusBar.setProgress(Integer.parseInt(maxDistance));
        mMaxRadiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mMaxRadiusDisplay.setText(""+progress+" km");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        chosenPlaceName = EasyReadSingleton.getInstance().getSearchLocationName();
        chosenPlaceLatLng = EasyReadSingleton.getInstance().getSearchLocationLatLng();


       mAutoCompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.pref_dialog_place_auto);
        if (chosenPlaceName != null){
            mAutoCompleteFragment.setText(chosenPlaceName);
        }
        mAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                chosenPlaceName = place.getName().toString();
                chosenPlaceLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
            }
        });

        builder.setTitle("Search Preferences")
                .setView(v)
                .setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        EasyReadSingleton instance = EasyReadSingleton.getInstance();
                        instance.setSearchMaxDistance(Integer.toString(mMaxRadiusBar.getProgress()));
                        boolean newLocation =false;
                        if(chosenPlaceName != null){
                            instance.setSearchLocationName(chosenPlaceName);
                            instance.setSearchLocationLatLng(chosenPlaceLatLng);
                            newLocation = true;
                        }
                        mListener.onPreferenceChange(SearchPreferencesDialog.this,newLocation);
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface d, int arg1) {
                d.cancel();
            };
        });

        return builder.create();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(mAutoCompleteFragment !=null){
            getFragmentManager().beginTransaction().remove(mAutoCompleteFragment).commit();
        }
    }

}
