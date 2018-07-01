package ng.petersabs.dev.crumby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewCrumbsAdapter extends RecyclerView.Adapter<ViewCrumbsAdapter.CrumbsViewHolder> {
    private View mSelectedItem = null;
    private ArrayList<HashMap> mCrumbsForToday;
    private String mCurrentDateString;
    private ViewCrumbs parent;

    ViewCrumbsAdapter(ArrayList<HashMap> Crumbs, String mCurrentDate, ViewCrumbs parent) {
        mCrumbsForToday = Crumbs;
        this.parent = parent;
        mCurrentDateString = mCurrentDate;
    }

    private static String capitalize(String input) {
        if (input != null && !input.isEmpty()) {
            String[] words = input.toLowerCase().split(" ");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                String word = words[i];

                if (i > 0 && word.length() > 0) {
                    builder.append(" ");
                }

                String cap = word.substring(0, 1).toUpperCase() + word.substring(1);
                builder.append(cap);
            }
            return builder.toString();
        }
        return "";
    }

    public void updateData(ArrayList<HashMap> Crumbs) {
        this.mCrumbsForToday.clear();
        this.mCrumbsForToday.addAll(Crumbs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CrumbsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int crumbItemID = R.layout.single_crumb_view;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(crumbItemID, parent, false);
        CrumbsViewHolder mCrumbsViewHolder = new CrumbsViewHolder(view);
        return mCrumbsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CrumbsViewHolder holder, int position) {
        holder.bootstrapItem(position);
    }

    @Override
    public int getItemCount() {
        return mCrumbsForToday.size();
    }

    class CrumbsViewHolder extends RecyclerView.ViewHolder {
        final TextView mCrumbTitleTextView;
        final TextView mCrumbDetailsTextView;
        final FloatingActionButton mDeleteFab;
        HashMap crumb;
        private String mCrumbTitle;
        private String mCrumbDetails;

        CrumbsViewHolder(final View itemView) {
            super(itemView);

            final Context context = itemView.getContext();
            mCrumbDetailsTextView = itemView.findViewById(R.id.crumb_detail_textview);
            mCrumbTitleTextView = itemView.findViewById(R.id.crumb_title_textview);
            mDeleteFab = itemView.findViewById(R.id.delete_crumb);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holdSelected(itemView);
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeSelected();
                    Intent addOrEditIntent = new Intent(context, AddOrEditCrumb.class);
                    addOrEditIntent.putExtra("current_date", mCurrentDateString);
                    addOrEditIntent.putExtra("title", mCrumbTitle);
                    addOrEditIntent.putExtra("crumb_text", mCrumbDetails);
                    ((Activity) context).startActivityForResult(addOrEditIntent, 22);
                }
            });
        }

        void holdSelected(View item) {
            View mSelected = getSelectedItem();
            if (mSelected != null) {
                mSelected.setElevation(5);
                mSelected.findViewById(R.id.delete_crumb).setVisibility(View.GONE);
                removeSelected();
            }
            mSelectedItem = item;
            item.findViewById(R.id.delete_crumb).setVisibility(View.VISIBLE);
            item.setElevation(15);
        }

        void removeSelected() {
            View mSelected = getSelectedItem();
            if (mSelected != null) {
                mSelected.setElevation(5);
                mSelected.findViewById(R.id.delete_crumb).setVisibility(View.GONE);
            }
        }

        View getSelectedItem() {
            return mSelectedItem;
        }

        void bootstrapItem(final int position) {
            crumb = mCrumbsForToday.get(position);
            mCrumbTitle = (String) crumb.get("title");
            mCrumbDetails = (String) crumb.get("crumb");
            mCrumbDetailsTextView.setText(mCrumbDetails);
            mCrumbTitleTextView.setText(capitalize(mCrumbTitle));
            mDeleteFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (parent.deleteCrumb(position)) {
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
