package elapse.easyread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by Joshua on 11/15/2017.
 */

public class SearchTermsDialog extends DialogFragment {
    NoticeSearchDialogListener mListener;
    private String selectedSort;
    private Context ctx;


    public interface NoticeSearchDialogListener{
        public void onSearch(DialogFragment dialog, String searchTerms,String sort);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_search_terms,null);

        final EditText mSearchTermsView = (EditText) v.findViewById(R.id.search_terms);
        final Spinner mSortView = (Spinner) v.findViewById(R.id.sort_list);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ctx,R.array.sort_options,android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortView.setAdapter(adapter);

        builder.setTitle("Exchange Search")
                .setView(v)
                .setPositiveButton("Search",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id){
                        mListener.onSearch(SearchTermsDialog.this,mSearchTermsView.getText().toString(),mSortView.getSelectedItem().toString());
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
            ctx = activity;
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeSearchDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
