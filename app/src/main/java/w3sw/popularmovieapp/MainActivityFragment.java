package w3sw.popularmovieapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**Main activity consist of grid layout that diplay the particular movie poster
 *sorted either by popularity or by rating. The main screen can be scrolled down
 *to view even more popular movie. If the user click one of the moive poster it
 * will start detail activity screen.
 */

/**Basic Function:
 *1.Present the user with a grid arrangement of movie posters upon launch.
 *2.Allow your user to change sort order via a setting:
 *3.The sort order can be by most popular or by highest-rated
 *4.Allow the user to tap on a movie poster and transition to a details screen with additional information such as:
 *  -original title
 *  -movie poster image thumbnail
 *  -A plot synopsis (called overview in the api)
 *  -user rating (called vote_average in the api)
 *  -release date
 */

public class MainActivityFragment extends Fragment {
    public static final String LOG_TAG = MainActivityFragment.class.toString();

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        UpdateMovie();
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id  = item.getItemId();
        if(id == R.id.action_refresh){
            UpdateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void UpdateMovie(){
        String search_type = "search_type";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String searchParameter = prefs.getString(search_type
                ,getString(R.string.search_setting_default));
        Log.v("Search Parameter: ",searchParameter);
        FetchMovie fetchMovie = new FetchMovie();
        fetchMovie.execute(searchParameter.toLowerCase());
    }

    public class FetchMovie extends AsyncTask<String, Void,
            ArrayList<HashMap<String,String>> > {
        JSONArray movieArray;

        // These are the names of the JSON objects that need to be extracted.
        // Its include the unused name for the expansion purpose
        final String RESULT = "results";
        final String POSTER_PATH = "poster_path";
        final String ADULT = "adult";
        final String OVERVIEW = "overview";
        final String RELEASE_DATE = "release_date";
        final String GENRE_IDS = "genre_ids";
        final String ID = "id";
        final String ORIGINAL_TITLE = "original_title";
        final String ORIGINAL_LANGUAGE = "original_language";
        final String TITLE = "title";
        final String BACKDROP_PATH ="backdrop_path";
        final String POPULARITY = "popularity";
        final String VOTE_COUNT = "vote_count";
        final String VIDEO = "video";
        final String VOTE_AVERAGE = "vote_average";

        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(String... params) {

            //These two need to be declared outside the try/catch
            //so they can be closed in the finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;



            //URL query parameter
            final String API_KEY = "122c7816bb3759bd56be1d03a02d6db6";
            final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";

            //Will contain the string of the json result
            String jsonresult = null;

            //Construct URL for the query search
            Uri builtUri = Uri.parse(MOVIE_BASE_URL + params[0]).buildUpon()
                    .appendQueryParameter("api_key", API_KEY)
                    .build();

            try {
                URL url = new URL(builtUri.toString());
                Log.v("URL link",url.toString());
                //Crete the request to themoviedb and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Read the input stream into string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if(inputStream == null){
                    //Nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                }
                if(buffer.length() ==0){
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonresult = buffer.toString();
                Log.v(LOG_TAG,jsonresult);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG,e.getMessage(),e);
                        e.printStackTrace();
                    }
                }
            }
            try {
                return GetMovieDataFromJson(jsonresult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(ArrayList<HashMap<String,String>> resultSet) {
            GridView gridView= (GridView)getActivity().findViewById(R.id.fragment_main);
            ArrayList<Uri> posterUrl = null;
            try {
                posterUrl = GetMoviePosterLink(resultSet);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ImageAdapter imageAdapter = new ImageAdapter(getActivity(),posterUrl);
            gridView.setAdapter(imageAdapter);
        }

        private ArrayList<Uri> GetMoviePosterLink(ArrayList<HashMap<String,String>> arrayList) throws JSONException {
            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p";
            final String SIZE = "w185";
            ArrayList<Uri> posterUriArray=new ArrayList<>();
            Log.v("Array List Size", String.valueOf(arrayList.size()));
            //get the url path for each movie and put it to its ImageView
            for(int i=0; i<arrayList.size();i++){
                //Construct URL for the query search
                Uri posterUrl = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendPath(SIZE)
                        .appendEncodedPath(arrayList.get(i).get(POSTER_PATH))
                        .build();
                Log.v("Poster Link:", posterUrl.toString());
                posterUriArray.add(posterUrl);
            }
            return posterUriArray;
        }


        private ArrayList<HashMap<String,String>> GetMovieDataFromJson(String movieJsonString)throws JSONException{

            JSONObject jsonObject = new JSONObject(movieJsonString);
            movieArray = jsonObject.getJSONArray(RESULT);
            ArrayList<HashMap<String,String>> resultArray = new ArrayList<>();

            //get the poster path and movie title for each movie
            for (int i = 0; i <movieArray.length(); i++){
                JSONObject movieObject = movieArray.getJSONObject(i);

                //Parsing Json to get the poster path and title
                String posterPath = movieObject.getString(POSTER_PATH);
                String movieTitle = movieObject.getString(TITLE);

                //Put the data to the HashMap
                HashMap<String,String> resultSet = new HashMap<>();
                resultSet.put(POSTER_PATH, posterPath);
                resultSet.put(TITLE,movieTitle);
                resultArray.add(resultSet);


                Log.v("Movie Title: ", resultArray.get(i).get(TITLE));
            }
            return resultArray;
        }

        private class ImageAdapter extends ArrayAdapter{
            private Context mContext;
            private ArrayList<Uri> url;
            LayoutInflater inflater;

            public ImageAdapter(Context context, ArrayList<Uri> imageUrl){
                super(context,R.layout.movie_poster,imageUrl);
                this.mContext = context;
                this.url = imageUrl;
                inflater = LayoutInflater.from(context);
            }

            @Override
            public int getCount() {
                return movieArray.length();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView = (ImageView) convertView;
                if(imageView == null){
                    imageView = (ImageView) inflater.inflate(R.layout.movie_poster,parent,false);
                }
                imageView.setVisibility(View.VISIBLE);
                Picasso
                        .with(mContext)
                        .load(url.get(position))
                        .resize(200, 400)
                        .into(imageView);
                return imageView;
            }
        }
    }



}
