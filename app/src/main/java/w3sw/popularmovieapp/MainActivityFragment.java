package w3sw.popularmovieapp;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
