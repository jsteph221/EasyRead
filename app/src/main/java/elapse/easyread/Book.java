package elapse.easyread;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Joshua on 2017-10-09.
 */

public class Book implements Parcelable {
    private String title;
    private String author;
    private String imageUrl;

    public Book(String title, String author,String imageUrl){
        this.title=title;
        this.author = author;
        this.imageUrl = imageUrl;
    }

    public Book(JSONObject book){
        this.title = book.optString("title");
        this.author = book.optString("author");
        this.imageUrl = book.optString("imageUrl");
    }


    public Book(Parcel source) {
        title = source.readString();
        author = source.readString();
        imageUrl = source.readString();
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor(){
        return author;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public JSONObject toJsonBook() throws JSONException{
        JSONObject b = new JSONObject();
        b.put("title",title);
        b.put("author",author);
        b.put("imageUrl",imageUrl);
        return b;
    }

    public static CharSequence[] toCharSequence(ArrayList<Book> books){
        ArrayList<String> bookStrings = new ArrayList<>();
        for (Book b : books){
            String toAdd = b.getTitle()+" By "+ b.getAuthor();
            bookStrings.add(toAdd);
        }
        return bookStrings.toArray(new CharSequence[bookStrings.size()]);
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(imageUrl);
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }

        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }
    };
}
