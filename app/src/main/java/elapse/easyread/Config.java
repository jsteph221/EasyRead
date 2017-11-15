package elapse.easyread;

/**
 * Config class for server api and google api
 */

public class Config {
    public static final String GOOGLEBOOKSAPI = "https://www.googleapis.com/books/v1/volumes?fields=totalItems,items(id,volumeInfo/title,volumeInfo/authors,volumeInfo/industryIdentifiers,volumeInfo/imageLinks/smallThumbnail)&q=";
    public static final String API = "http://192.168.1.69:3000/api/";
}
