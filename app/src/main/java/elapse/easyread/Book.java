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
    private String isbn; //isbn 10

    public Book(String title, String author,String imageUrl,String isbn){
        this.title=title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.isbn = isbn;
    }

    public Book(JSONObject book){
        this.title = book.optString("title");
        this.author = book.optString("author");
        this.imageUrl = book.optString("imageUrl");
        this.isbn = book.optString("isbn");
    }


    public Book(Parcel source) {
        title = source.readString();
        author = source.readString();
        imageUrl = source.readString();
        isbn = source.readString();
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
    public String getIsbn(){return isbn;}

    public JSONObject toJsonBook() throws JSONException{
        JSONObject b = new JSONObject();
        b.put("title",title);
        b.put("author",author);
        b.put("imageUrl",imageUrl);
        b.put("isbn",isbn);
        return b;
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
        dest.writeString(isbn);
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
