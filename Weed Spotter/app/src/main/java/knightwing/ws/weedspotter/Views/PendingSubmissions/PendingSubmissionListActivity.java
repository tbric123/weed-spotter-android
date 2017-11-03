package knightwing.ws.weedspotter.Views.PendingSubmissions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import knightwing.ws.weedspotter.R;

import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionCell;
import knightwing.ws.weedspotter.Views.Activities.SubmissionStages.StartupScreenActivity;

import java.util.List;

/**
 * List of all submissions that need to be sent.
 */
public class PendingSubmissionListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_submissions_list);

        // Pending Submissions - title of the list of pending submissions
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getTitle());

        // Construct the layout of the pending submissions list
        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        // Adapt for larger screens and tablets
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }
    }

    /**
     * Go back to the start of the app.
     * @param v - the view where the call to go to the start is being made
     * @require v != null
     * @ensure the start screen is seen
     */
    public void goToStart(View v) {
        startActivity(new Intent(PendingSubmissionListActivity.this, StartupScreenActivity.class));
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(PendingSubmissionCell.ITEMS));
    }

    // Placeholder code as part of a Master-Detail View
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<PendingSubmissionCell.PendingSubmission> mValues;

        public SimpleItemRecyclerViewAdapter(List<PendingSubmissionCell.PendingSubmission> items) {
            mValues = items;
            this.notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.pending_submission_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(String.valueOf(mValues.get(position).id));
            holder.mContentView.setText(mValues.get(position).getListEntry());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PendingSubmissionDetailFragment.itemID = String.valueOf(position + 1);
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(PendingSubmissionDetailFragment.ARG_ITEM_ID,
                                String.valueOf(holder.mItem.id));
                        PendingSubmissionDetailFragment fragment = new PendingSubmissionDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, PendingSubmissionDetailActivity.class);
                        intent.putExtra(PendingSubmissionDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        /**
         * Design of a submission row.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public PendingSubmissionCell.PendingSubmission mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
