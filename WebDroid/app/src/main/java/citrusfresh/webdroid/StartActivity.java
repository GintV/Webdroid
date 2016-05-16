package citrusfresh.webdroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

public class StartActivity extends FragmentActivity implements InfoFragment.OnInfoConfirmListener, ConnectionChoiceFragment.OnChoiceListener, CodeEnterFragment.OnCodeEnteredListener, QRcodeScanFragment.OnQRScannedListener {

    private boolean nextActivityStarted;
    private final Object lock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_start);

        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            InfoFragment firstFragment = new InfoFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nextActivityStarted = false;
    }


    @Override
    public void onInfoConfirm() {
        makeTransaction(new ConnectionChoiceFragment(), "choice");
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void makeTransaction(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onChoiceMade(int choice) {
        switch (choice) {
            case 1:
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        askForPermission();
                    }
                    return;
                }
                makeTransaction(new QRcodeScanFragment(), "QR");
                break;
            default:
                makeTransaction(new CodeEnterFragment(), "input");
                break;
        }
    }

    @Override
    public void onCodeEntered(String code) {
        switchToGameActivity(code);
    }

    @Override
    public void onQRScanned(String code) {
        switchToGameActivity(code);
    }

    @TargetApi(23)
    protected boolean askForPermission() {
        this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        return true;
    }

    protected void switchToGameActivity(String code) {
        boolean allowed = false;
        synchronized (lock) {
            if (!nextActivityStarted) {
                nextActivityStarted = true;
                allowed = true;
            }
        }
        if (allowed) {
            Intent intent = new Intent(this, GameActivity.class);
            String keyIdentifier = "sessionId";
            intent.putExtra(keyIdentifier, code);
            startActivity(intent);
        }
    }
}
