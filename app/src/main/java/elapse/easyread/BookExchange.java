package elapse.easyread;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Joshua on 2017-10-07.
 */

public class BookExchange {
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

    public BookExchange(JSONObject exchange){
        this.posterId = exchange.optString("posterId");
        this.locationName = exchange.optString("locationName");
        this.description = exchange.optString("description");
        JSONArray loc = exchange.optJSONArray("locationData");
        this.latLng = new LatLng(loc.optDouble(1),loc.optDouble(0));
        this.date = exchange.optString("date");
        try{
            this.book = new Book(exchange.getJSONArray("book").getJSONObject(0));
        }catch(JSONException e){
            Log.d("Error: ",e.getMessage());
        }
    }

    public String getPosterId(){
        return posterId;
    }
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

}
