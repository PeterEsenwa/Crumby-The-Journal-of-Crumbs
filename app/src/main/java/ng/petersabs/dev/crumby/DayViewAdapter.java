package ng.petersabs.dev.crumby;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.DayViewHolder> {
    //    public int mTotalDayItems;
//    int currentDayItem;
    private DayViewHolder dayViewHolder;
    private ArrayList<HashMap> Crumbs;
    private Context context;

    DayViewAdapter(ArrayList<HashMap> Crumbs, Context context) {
        this.Crumbs = Crumbs;
        this.context = context;
    }

    public void updateData(ArrayList<HashMap> Crumbs) {
        this.Crumbs.clear();
        this.Crumbs.addAll(Crumbs);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bootstrapItem();
    }


    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int dayItemID = R.layout.content_crumbs;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(dayItemID, parent, false);
        dayViewHolder = new DayViewHolder(view);
        return dayViewHolder;
    }

    public void addDayViewItem(int numberOfViews) {
        mItemCount += numberOfViews;
    }


    // Show ten day view items at first
    private int mItemCount = 10;

    /* public void addItem(int position, ViewModel viewModel) {
         items.add(position, viewModel);
         notifyItemInserted(position);
     }

     public void removeItem(int position) {
         items.remove(position);
         notifyItemRemoved(position);
     }*/
    class DayViewHolder extends RecyclerView.ViewHolder {
        final TextView mCurrentDateView;
        final TextView mCurrentDayView;
        final TextView mNoOfCrumbsInt;
        final TextView mNoOfCrumbsString;
        final TextView mReadOrAdd;
        final RelativeLayout mGotoViewCrumbs;
        final DateFormat dateFormat =
                new SimpleDateFormat("dd MMMM", Locale.getDefault());
        final DateFormat dayFormat =
                new SimpleDateFormat("E", Locale.getDefault());
        int mCurrentAdapterPosition;
        private ArrayList<HashMap> mCrumbsOnDate;

        DayViewHolder(View itemView) {
            super(itemView);
//            Date yourDate = DateUtils.
            mCurrentDateView = itemView.findViewById(R.id.day_date_view);
            mCurrentDayView = itemView.findViewById(R.id.day_info);
            mNoOfCrumbsInt = itemView.findViewById(R.id.no_of_crumbs_number);
            mNoOfCrumbsString = itemView.findViewById(R.id.no_of_crumbs_string);
            mGotoViewCrumbs = itemView.findViewById(R.id.goto_view_crumbs);
            mReadOrAdd = itemView.findViewById(R.id.reminisce_textview);
        }

        int getCrumbsForDate(Date mDayViewDate) {
            try {
                int mTotalOfCrumbs = 0;
                for (HashMap crumb : Crumbs) {
                    Date mCurrentCrumbDate = (Date) crumb.get("date");
                    if (mDayViewDate.getDate() == mCurrentCrumbDate.getDate()) {
                        mCrumbsOnDate.add(crumb);
                        ++mTotalOfCrumbs;
                    }
                }
                return mTotalOfCrumbs;
            } catch (Exception ex) {
                Log.e("Error occurred : ", ex.getMessage());
                return 0;
            }
        }

        void bootstrapItem() {
            mCrumbsOnDate = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            mCurrentAdapterPosition = this.getAdapterPosition();
            calendar.add(Calendar.DATE, 0 - this.getAdapterPosition());
            Date curDate = calendar.getTime();
            int noOfCrumbsOnDate = getCrumbsForDate(curDate);
            final String currentDateFormatted = dateFormat.format(curDate);
            String currentDayFormatted = dayFormat.format(curDate);

            currentDayFormatted += " - " + getDateDiff(curDate);

            mCurrentDateView.setText(currentDateFormatted);
            mCurrentDayView.setText(currentDayFormatted);
            mNoOfCrumbsInt.setText(String.valueOf(noOfCrumbsOnDate));
            if (noOfCrumbsOnDate == 0) {
                mNoOfCrumbsString.setText(context.getResources().getString(R.string.no_crumbs_found));
                mReadOrAdd.setText(R.string.prompt_leave_crumbs);
            } else {
                mNoOfCrumbsString.setText(context.getResources().getString(R.string.no_of_crumbs_found, noOfCrumbsOnDate));
            }

            mGotoViewCrumbs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent viewDayCrumbs = new Intent(context, ViewCrumbs.class);
                    viewDayCrumbs.putExtra("current_day", currentDateFormatted);
                    viewDayCrumbs.putExtra("all_crumbs_today", mCrumbsOnDate);
                    viewDayCrumbs.putExtra("position", mCurrentAdapterPosition);
                    viewDayCrumbs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(viewDayCrumbs);
                }
            });
        }

        private String getDateDiff(Date curDate) {
            Date realCurrentDate = new Date();
            long dateDiff = realCurrentDate.getTime() - curDate.getTime();
            dateDiff = TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);

            if (dateDiff <= 0)
                return "Today";
            else if (dateDiff <= 1)
                return "Yesterday";
            else
                return dateDiff + " days ago";
        }
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }
}
