package elapse.easyread;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

/**
 * Created by Joshua on 2017-10-07.
 */

public class ListViewAdapter extends ArrayAdapter {
    private ArrayList<BookExchange> exchanges;
    private Context ctx;

    public ListViewAdapter(Context context, int textViewResourceId, ArrayList<BookExchange> items) {
        super(context, textViewResourceId, items);
        this.exchanges = items;
        this.ctx = context;
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
            TextView posterNameText = (TextView) v.findViewById(R.id.list_poster_name);
            CustomNetworkImageView bookPicture = (CustomNetworkImageView) v.findViewById(R.id.list_book_image);
            TextView exchangeDate = (TextView) v.findViewById(R.id.list_exchange_date);
            ImageButton goButton = (ImageButton) v.findViewById(R.id.list_go);

            if (bookTitleText != null) {
                bookTitleText.setText(exchanges.get(position).getBook().getTitle());
            }
            if (bookAuthorText != null) {
                bookAuthorText.setText("Author : "+ exchanges.get(position).getBook().getAuthor());
            }
            if (posterNameText != null) {
                posterNameText.setText("Poster : " + exchanges.get(position).getPosterId());
            }
            if (bookPicture != null) {
                bookPicture.setImageUrl(exchanges.get(position).getBook().getImageUrl(),EasyReadSingleton.getInstance(this.getContext()).getImageLoader());
            }
            if(exchangeDate != null){
                exchangeDate.setText(exchanges.get(position).getDate());
            }
            final int pos = position;
            goButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ctx,ShowExchangeActivity.class);
                    i.putExtra("book_exchange",exchanges.get(pos));
                    ctx.startActivity(i);
                }
            });
        }

        return v;
    }
}
