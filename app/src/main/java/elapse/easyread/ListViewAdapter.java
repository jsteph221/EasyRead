package elapse.easyread;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

/**
 * Created by Joshua on 2017-10-07.
 */

public class ListViewAdapter extends ArrayAdapter {
    private ArrayList<BookExchange> exchanges;

    public ListViewAdapter(Context context, int textViewResourceId, ArrayList<BookExchange> items) {
        super(context, textViewResourceId, items);
        this.exchanges = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_element, null);
        }
        BookExchange exch = exchanges.get(position);
        if (exch != null) {
            TextView bookTitleText = (TextView) v.findViewById(R.id.list_book_title);
            TextView bookAuthorText = (TextView) v.findViewById(R.id.list_book_author);
            TextView posterNameText = (TextView) v.findViewById(R.id.poster_name);
            NetworkImageView bookPicture = (NetworkImageView) v.findViewById(R.id.list_book_image);

            if (bookTitleText != null) {
                bookTitleText.setText(exchanges.get(position).getBook().getTitle());
            }
            if (bookAuthorText != null) {
                bookAuthorText.setText(exchanges.get(position).getBook().getAuthor());
            }
            if (posterNameText != null) {
                posterNameText.setText(exchanges.get(position).getPoster());
            }
            if (bookPicture != null) {
                bookPicture.setImageUrl(exchanges.get(position).getBook().getImageUrl(),EasyReadSingleton.getInstance(this.getContext()).getImageLoader());
            }
        }

        return v;
    }
}
