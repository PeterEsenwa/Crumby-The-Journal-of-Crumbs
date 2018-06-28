package ng.petersabs.dev.crumby;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DayViewAdapter extends RecyclerView.Adapter<DayViewAdapter.DayViewHolder> {
    private JSONObject mSomeDays;
//    public int mTotalDayItems;
//    int currentDayItem;
private DayViewHolder dayViewHolder;

    DayViewAdapter(JSONObject mSomeDays) {
        this.mSomeDays = mSomeDays;
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        TextView mCurrentDateView;
        TextView mCurrentDayView;
        DateFormat dateFormat =
                new SimpleDateFormat("dd MMMM", Locale.getDefault());
        DateFormat dayFormat =
                new SimpleDateFormat("E", Locale.getDefault());

        public DayViewHolder(View itemView) {
            super(itemView);
//            Date yourDate = DateUtils.
            mCurrentDateView = itemView.findViewById(R.id.day_date_view);
            mCurrentDayView = itemView.findViewById(R.id.day_info);
        }

        void bootstrapItem(int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 0 - this.getAdapterPosition());
            Date curDate = calendar.getTime();
            String currentDateFormatted = dateFormat.format(curDate);
            String currentDayFormatted = dayFormat.format(curDate);

            currentDayFormatted += " - " + getDateDiff(curDate);

            mCurrentDateView.setText(currentDateFormatted);
            mCurrentDayView.setText(currentDayFormatted);
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

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
//        lastDayItemListener.onLastItem(position);
        holder.bootstrapItem(position);
    }

    // Show ten day view items at first
    private int mItemCount = 10;

    public void addDayViewItem(int numberOfViews){
        mItemCount+= numberOfViews;
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }
}
