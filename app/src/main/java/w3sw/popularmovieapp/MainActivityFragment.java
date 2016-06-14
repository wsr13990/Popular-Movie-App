package w3sw.popularmovieapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
        FetchMovie fetchMovie = new FetchMovie();
        fetchMovie.execute("/movie/popular");
    }

    public class FetchMovie extends AsyncTask<String, Void, HashMap<String,String>> {
        // These are the names of the JSON objects that need to be extracted.
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
        protected HashMap<String,String> doInBackground(String... params) {

            //These two need to be declared outside the try/catch
            //so they can be closed in the finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //Will contain the string of the json result
            String jsonresult = null;

            //URL query parameter
            final String POPULAR_MOVIE = "/movie/popular";
            final String TOP_RATED_MOVIE = "/movie/top_rated";

            final String API_KEY = "122c7816bb3759bd56be1d03a02d6db6";
            final String MOVIE_BASE_URL = "https://api.themoviedb.org/3";

            //Construct URL for the query search
            Uri builtUri = Uri.parse(MOVIE_BASE_URL + params[0]).buildUpon()
                    .appendQueryParameter("api_key", API_KEY)
                    .build();
            try {
                URL url = new URL(builtUri.toString());
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
                    buffer.append(line +"\n");
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
                return GetMovieDataFromJson(jsonresult,0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(HashMap<String,String> resultSet) {
            Log.v("Result HashMap",resultSet.get(POSTER_PATH));
            try {
                GetMoviePoster(resultSet.get(POSTER_PATH), 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void GetMoviePoster(String path, int i) throws JSONException {
            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p";
            final String SIZE = "w185";
            //Construct URL for the query search
            Uri posterUrl = Uri.parse(POSTER_BASE_URL).buildUpon()
                    .appendPath(SIZE)
                    .appendEncodedPath(path)
                    .build();
            Log.v("Poster Link:",posterUrl.toString());
            //Display the movie poster to the ImageView
            ImageView posterView = (ImageView)getActivity().findViewById(R.id.movie_poster_main);
            Picasso.with(getActivity()).load(posterUrl)
                    .into(posterView);
        }
        private HashMap<String,String> GetMovieDataFromJson(String movieJsonString, int num)throws JSONException{

            JSONObject jsonObject = new JSONObject(movieJsonString);
            JSONArray movieArray = jsonObject.getJSONArray(RESULT);
            JSONObject movieObject = movieArray.getJSONObject(num);
            //Parsing Json to get the poster path and title
            String posterPath = movieObject.getString(POSTER_PATH);
            String movieTitle = movieObject.getString(TITLE);

            HashMap<String,String> resultSet = new HashMap<>();
            resultSet.put(POSTER_PATH, posterPath);
            resultSet.put(TITLE,movieTitle);
            Log.v("Movie Title: ", resultSet.get(TITLE));
            return resultSet;
        }
    }



}
