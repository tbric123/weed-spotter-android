package knightwing.ws.weedspotter.Models.PendingSubmissions;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tbric123 on 20/9/17.
 * Helper class for specifying the pending submission database's version and name and
 * handling its creation and upgrading.
 */

public class PendingSubmissionStorageHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String STORAGE_NAME = "PendingSubmissionStorage.db";

    public PendingSubmissionStorageHelper(Context context) {
        super(context, STORAGE_NAME, null, VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PendingSubmissionStorage.PendingSubmissionEntry.getTableCreationQuery());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(PendingSubmissionStorage.PendingSubmissionEntry.getTableDeletionQuery());
        onCreate(db);
    }

}
