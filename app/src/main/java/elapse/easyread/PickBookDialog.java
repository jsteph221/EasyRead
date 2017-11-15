package elapse.easyread;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

/**
 * Created by Joshua on 2017-10-10.
 */

public class PickBookDialog extends DialogFragment {
        private Context ctx;

        public interface NoticeDialogListener {
        public void onBookClick(DialogFragment dialog,Book clickedBook);
    }
        NoticeDialogListener mListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ArrayList<Book> books = getArguments().getParcelableArrayList("foundBooks");
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_pick_book_title)
                    .setAdapter(new AddDialogListAdapter(ctx,R.id.list_add_title,books), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onBookClick(PickBookDialog.this,books.get(which));
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            // Verify that the host activity implements the callback interface
            try {
                ctx = activity;
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (NoticeDialogListener) activity;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activity.toString()
                        + " must implement NoticeDialogListener");
            }
        }
        class AddDialogListAdapter extends ArrayAdapter {
            private ArrayList<Book> books;
            private Context ctx;

            public AddDialogListAdapter(Context context, int textViewResourceId, ArrayList<Book> items) {
                super(context, textViewResourceId, items);
                this.books = items;
                this.ctx = context;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.list_element_add_book, null);
                }
                Book b = books.get(position);
                if (b != null) {
                    TextView mBookTitle = (TextView) v.findViewById(R.id.list_add_title);
                    TextView mBookAuthor = (TextView) v.findViewById(R.id.list_add_author);
                    TextView mBookIsbn = (TextView) v.findViewById(R.id.list_add_isbn);
                    CustomNetworkImageView mBookImg = (CustomNetworkImageView) v.findViewById(R.id.list_add_image);
                    if(mBookTitle != null){
                        mBookTitle.setText(b.getTitle());
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
