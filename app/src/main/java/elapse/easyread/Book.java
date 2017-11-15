package elapse.easyread;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Object for holding a information on a book
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

    //Constructor for JSON response from server

    public Book(JSONObject book){
        this.title = book.optString("title");
        this.author = book.optString("author");
        this.imageUrl = book.optString("imageUrl");
        this.isbn = book.optString("isbn");
    }

    //Parcable for passing object through intent
    private Book(Parcel source) {
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
    public String getIsbn(){return isbn != null ? isbn : "";}
    public void setImageUrl(String url){this.imageUrl = url;}

    //Creating JSONobject to send to server

    public JSONObject toJsonBook() throws JSONException{
        JSONObject b = new JSONObject();
        b.put("title",title);
        b.put("author",author);
        b.put("imageUrl",imageUrl);
        b.put("isbn",isbn);
        return b;
    }

    //Parcelable methods
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
