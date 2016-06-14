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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
        fetchMovie.execute();
    }

    public class FetchMovie extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

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
            Uri builtUri = Uri.parse(MOVIE_BASE_URL + POPULAR_MOVIE).buildUpon()
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
                StringBuffer buffer = new StringBuffer();
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

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
