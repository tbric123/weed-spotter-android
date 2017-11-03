package knightwing.ws.weedspotter.Views.Activities.SubmissionStages;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * The screen first seen before the start screen appears, while the app is loading.
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, StartupScreenActivity.class));
        finish();
    }

    /**
     * Stop back button from being able to be pressed.
     * https://stackoverflow.com/questions/20623659/disable-back-button-in-android
     */
    @Override
    public void onBackPressed() {}
}
