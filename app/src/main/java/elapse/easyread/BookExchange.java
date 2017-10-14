package elapse.easyread;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Joshua on 2017-10-07.
 */

public class BookExchange {
    private String poster;
    private Book book;
    private String locationName;
    private LatLng latLng;
    private String description;

    public BookExchange(Book b, String poster, String locName,LatLng latLng,String desc){
        this.book=b;
        this.poster=poster;
        this.locationName = locName;
        this.latLng = latLng;
        this.description = desc;

    }

    public String getPoster(){
        return poster;
    }
    public Book getBook(){
        return book;
    }
    public JSONObject toJsonBookExchange(){
        JSONObject req = new JSONObject();
        double[] location = new double[2];
        location[0] = latLng.longitude;
        location[1] = latLng.latitude;
        try{
            req.put("book",book.toJsonBook());
            req.put("posterId",poster);
            req.put("locationData",location);
            req.put("locationName",locationName);
            req.put("description",description);
            return req;

        }catch (JSONException e){
            Log.d("BookExchange To Json",e.toString());
            return null;
        }

    }

}
