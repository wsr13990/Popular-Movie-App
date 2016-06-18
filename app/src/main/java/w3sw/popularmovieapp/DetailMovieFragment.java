package w3sw.popularmovieapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailMovieFragment extends Fragment {

    public DetailMovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_movie_fragment,container,false);

        //Initialize detail content variable
        TextView title = (TextView) rootView.findViewById(R.id.movie_title);
        ImageView poster = (ImageView)rootView.findViewById(R.id.movie_poster);
        TextView movieYear = (TextView)rootView.findViewById(R.id.movie_year);
        TextView synopis = (TextView)rootView.findViewById(R.id.movie_synopis);
        TextView rating = (TextView)rootView.findViewById(R.id.rating);

        //Assign the value of detail content
        Intent intent = getActivity().getIntent();
        ArrayList<String> extra = intent.getStringArrayListExtra(Intent.EXTRA_TEXT);
        title.setText(extra.get(0));
        Uri posterUri = Uri.parse(extra.get(1));
        Picasso.with(getActivity()).load(posterUri).fit().into(poster);
        movieYear.setText(extra.get(2));
        synopis.setText(extra.get(3));
        rating.setText(extra.get(4));

        Log.v("Movie Year: ", extra.get(2));
        Log.v("Rating: ",extra.get(4));

        return rootView;

    }
}
