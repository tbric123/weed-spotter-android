package knightwing.ws.weedspotter.Views.Activities.SubmissionStages;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.R;

/**
 * Screen displayed to the user on successful submission of a suspected weed.
 */
public class SubmissionCompleteActivity extends AppCompatActivity {

    CurrentSubmission currentSubmission = CurrentSubmission.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_complete);
    }

    /**
     * Bring user back to the beginning to carry out another submission.
     * @param view - the control that will have this action carried out when interacted with.
     * @require view != null
     * @ensure the user is returned to the start to try again.
     */
    public void startOver(View view) {
        currentSubmission.clearSubmission();
        startActivity(new Intent(SubmissionCompleteActivity.this, StartupScreenActivity.class));
    }

    /**
     * Stop back button from being able to be pressed.
     * https://stackoverflow.com/questions/20623659/disable-back-button-in-android
     */
    @Override
    public void onBackPressed() {}
}
