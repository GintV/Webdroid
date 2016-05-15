package citrusfresh.webdroid;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class GameActivity extends FragmentActivity implements SetUpFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (findViewById(R.id.fragment_container_game) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            SetUpFragment firstFragment = new SetUpFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_game, firstFragment).commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
