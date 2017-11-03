package knightwing.ws.weedspotter.Views.Activities.SubmissionStages;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionCell;
import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionStorage;
import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionStorageHelper;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;
import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Views.Extras.AppExtrasActivity;
import knightwing.ws.weedspotter.Views.PendingSubmissions.PendingSubmissionListActivity;

/*
 * The first screen you see when starting up the app.
 */
public class StartupScreenActivity extends AppCompatActivity {
    private ProgressBar startupBar;
    private TextView startupTextPrompt;
    private Button startButton;
    private Button viewPendingSubmissionsButton;
    private TextView pendingSubmissionsCounter;
    public PendingSubmissionStorageHelper pssHelper;
    public static PendingSubmissionStorageHelper pHelper;
    public static SQLiteDatabase pendingSubmissionsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_screen);
        pssHelper = new PendingSubmissionStorageHelper(getBaseContext());
        startupBar = (ProgressBar)(findViewById(R.id.startingUp));
        startupTextPrompt = (TextView)(findViewById(R.id.startingUpPrompt));
        startButton = (Button)(findViewById(R.id.btn_start));
        viewPendingSubmissionsButton = (Button)(findViewById(R.id.viewPendingSubmissions));
        startupBar.setVisibility(View.INVISIBLE);
        pHelper = pssHelper;

        // Count number of pending submissions and display on the start up screen
        PendingSubmissionCell.refreshItems();
        pendingSubmissionsDatabase = pHelper.getReadableDatabase();
        pendingSubmissionsCounter = (TextView)(findViewById(R.id.pendingSubmissionCount));
        int pendingSubmissionCount = PendingSubmissionStorage.PendingSubmissionEntry.getNumberOfSubmissions();

        pendingSubmissionsCounter.setText("Unsent Submissions: " + String.valueOf(pendingSubmissionCount));

        // Disable View Pending Submissions button if there aren't any to view
        if (pendingSubmissionCount == 0) {
            Utilities.setButtonEnabled(viewPendingSubmissionsButton, false);
        } else {
            Utilities.setButtonEnabled(viewPendingSubmissionsButton, true);
        }

    }

    /**
     * Begin the process of gathering weeds.
     * @param view - the control who will carry out this action when interacted with
     * @require view != null
     * @ensure the location finding step is the first to be seen
     */
    public void launchWizard(View view) {
        new StartupTask().execute();
    }

    /**
     * View all of the submissions that haven't been sent yet, as long as an Internet connection
     * is made.
     * @param view - the control who will carry out this action when interacted with
     * @require view != null
     * @ensure all of the submissions that haven't been sent yet are displayed
     */
    public void viewPendingSubmissions(View view) {
        startActivity(new Intent(StartupScreenActivity.this, PendingSubmissionListActivity.class));
    }

    /**
     * Access extras menu where app information, privacy policy and submission rules can be viewed,
     * as well as a quick tutorial on making a submission
     * @param view - the control who will carry out this action when interacted with
     * @require view != null
     * @ensure the following options are available: Quick Tutorial, Privacy Policy,
     *          Submission Rules, About
     */
    public void accessExtras(View view) {
        startActivity(new Intent(StartupScreenActivity.this, AppExtrasActivity.class));
    }

    /**
     * Places the starting up functionality in its own thread.
     */
    private class StartupTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            startupTextPrompt.setText("Starting up...");
            startupBar.setVisibility(View.VISIBLE);
            startupBar.setIndeterminate(true);
            startButton.setEnabled(false);
        }

        @Override
        protected void onPostExecute(String result) {
            startupTextPrompt.setText(result); // Will be empty at this time.
            startupBar.setVisibility(View.INVISIBLE);
            startButton.setEnabled(true);
        }
        @Override
        protected String doInBackground(Void... params) {
            startActivity(new Intent(StartupScreenActivity.this, CurrentLocationActivity.class));
            return "";
        }
    }

    public static PendingSubmissionStorageHelper getPssHelper() {
        return pHelper;
    }

    /**
     * Stop back button from being able to be pressed.
     * https://stackoverflow.com/questions/20623659/disable-back-button-in-android
     */
    @Override
    public void onBackPressed() {}
}
