package elapse.easyread;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Object to hold book exchange
 */

public class BookExchange implements Parcelable {
    private String id;
    private String posterId;
    private Book book;
    private String locationName;
    private LatLng latLng;
    private String description;
    private String date;

    public BookExchange(Book b, String posterId, String locName,LatLng latLng,String desc){
        this.book=b;
        this.posterId=posterId;
        this.locationName = locName;
        this.latLng = latLng;
        this.description = desc;
    }
    //Constructor from json response
    public BookExchange(JSONObject exchange){
        this.posterId = exchange.optString("posterId");
        this.locationName = exchange.optString("locationName");
        this.description = exchange.optString("description");
        JSONArray loc = exchange.optJSONArray("locationData");
        this.latLng = new LatLng(loc.optDouble(1),loc.optDouble(0));
        this.date = exchange.optString("date").substring(0,10);
        this.id = exchange.optString("_id");
        try{
            this.book = new Book(exchange.getJSONArray("book").getJSONObject(0));
        }catch(JSONException e){
            Log.d("Error: ",e.getMessage());
        }
    }
    public String getId(){return id;}
    public void setId(String id){this.id =id;}
    public BookExchange(Parcel  source){
        posterId = source.readString();
        book = source.readParcelable(Book.class.getClassLoader());
        locationName = source.readString();
        latLng = source.readParcelable(LatLng.class.getClassLoader());
        description = source.readString();
        date = source.readString();
        id = source.readString();
}

    public String getPosterId(){
        return posterId;
    }
    public String getDescription(){return description;}
    public String getLocationName(){return locationName;}
    public LatLng getLatLng(){return latLng;}

    public String getDate(){
        if(date != null) return date;
        return null;
    }
    public Book getBook(){
        return book;
    }
    public JSONObject toJsonBookExchange(){
        JSONObject req = new JSONObject();
        ArrayList<String> location = new ArrayList<>();
        location.add(Double.toString(latLng.longitude));
        location.add(Double.toString(latLng.latitude));
        try{
            req.put("book",book.toJsonBook());
            req.put("posterId",posterId);
            req.put("locationData",new JSONArray(location));
            req.put("locationName",locationName);
            req.put("description",description);
            return req;

        }catch (JSONException e){
            Log.d("BookExchange To Json",e.toString());
            return null;
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(posterId);
        dest.writeParcelable(book,0);
        dest.writeString(locationName);
        dest.writeParcelable(latLng,0);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeString(id);
    }

    public static final Creator<BookExchange> CREATOR = new Creator<BookExchange>() {
        @Override
        public BookExchange[] newArray(int size) {
            return new BookExchange[size];
        }

        @Override
        public BookExchange createFromParcel(Parcel source) {
            return new BookExchange(source);
        }
    };
}
