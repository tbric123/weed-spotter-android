package knightwing.ws.weedspotter.Views.PendingSubmissions;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionCell;

/**
 * Details for a single pending submission.
 */
public class PendingSubmissionDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    public static String itemID;
    private PendingSubmissionCell.PendingSubmission mItem;

    public PendingSubmissionDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PendingSubmissionCell.ITEM_MAP.containsKey(itemID)) {

            // Display the submission number as a title in the view showing the submission's details
            mItem = PendingSubmissionCell.ITEM_MAP.get(itemID);
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle("Submission #" + itemID);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pending_submission_detail, container, false);
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.details);
        }

        return rootView;
    }
}
