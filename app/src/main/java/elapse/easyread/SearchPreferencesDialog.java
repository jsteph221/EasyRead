package elapse.easyread;

import android.app.Dialog;
import android.app.DialogFragment;
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

/**
 * Created by Joshua on 10/17/2017.
 */

public class SearchPreferencesDialog extends DialogFragment {
    private Place chosenPlace;
    private PlaceAutocompleteFragment mAutoCompleteFragment;
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

        chosenPlace = EasyReadSingleton.getInstance().getSearchLocation();


       mAutoCompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.pref_dialog_place_auto);
        if (chosenPlace != null){
            mAutoCompleteFragment.setText(chosenPlace.getName());
        }
        mAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                chosenPlace = place;
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
                        if(chosenPlace != null){
                            instance.setSearchLocation(chosenPlace);
                        }
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
    public void onDestroyView(){
        super.onDestroyView();
        if(mAutoCompleteFragment !=null){
            getFragmentManager().beginTransaction().remove(mAutoCompleteFragment).commit();
        }
    }

}
