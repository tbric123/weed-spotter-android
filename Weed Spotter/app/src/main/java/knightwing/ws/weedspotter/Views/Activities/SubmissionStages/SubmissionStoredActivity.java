package knightwing.ws.weedspotter.Views.Activities.SubmissionStages;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionCell;
import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionStorage;
import knightwing.ws.weedspotter.R;

/**
 * Screen displayed to the user when they don't have an Internet Connection, storing the
 * submission created for sending later on.
 */
public class SubmissionStoredActivity extends AppCompatActivity {
    private CurrentSubmission currentSubmission = CurrentSubmission.getInstance();
    SQLiteDatabase pendingSubmissions = StartupScreenActivity.getPssHelper().getReadableDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_stored);

        // Add submission to the database
        storePendingSubmission(currentSubmission);
        PendingSubmissionCell.refreshItems();
        currentSubmission.clearSubmission();
    }

    /**
     * Stop back button from being able to be pressed.
     * https://stackoverflow.com/questions/20623659/disable-back-button-in-android
     */
    @Override
    public void onBackPressed() {}

    /**
     * Go back to the start screen.
     * @param v the view where the command to go to the start screen is being made.
     * @require v != null
     * @ensure start screen is clearly seen.
     */
    public void startOver(View v) {
        startActivity(new Intent(SubmissionStoredActivity.this, StartupScreenActivity.class));
    }

    /**
     * Store the pending submission in the database.
     * @param submission the submission being stored
     * @require submission != null
     * @return the ID of the submission that has just been stored
     */
    private long storePendingSubmission(CurrentSubmission submission) {
        ContentValues submissionValues = new ContentValues();
        submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.NAME_TITLE,
                submission.getSenderName());
        submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.SUBMISSION_DATE_TITLE,
                submission.getSubmissionDate());
        submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.PLANT_TYPE_TITLE,
                submission.getPlantType().toString());
        submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.PLANT_GROWTH_TITLE,
                submission.getPlantGrowth().toString());
        submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.PHOTOS_ZIP_TITLE,
                submission.getAttachmentPath().toString());

        // If GPS location is set, add the coordinates returned, else add dummy values
        if (submission.locationHasBeenSet()) {
            submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.LATITUDE_TITLE,
                    submission.getLatitude());
            submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.LONGITUDE_TITLE,
                    submission.getLongitude());
        } else {
            submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.LATITUDE_TITLE,
                    -200);
            submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.LONGITUDE_TITLE,
                    -200);
        }

        submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.CLOSEST_LOCATION_TITLE,
                submission.getClosestTownSuburb());
        submissionValues.put(PendingSubmissionStorage.PendingSubmissionEntry.NOTES_TITLE,
                submission.getNotes());
        return pendingSubmissions.insert(PendingSubmissionStorage.PendingSubmissionEntry.TABLE_TITLE,
                null, submissionValues);
    }
}
