package ng.petersabs.dev.crumby;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class CrumbsActivity extends AppCompatActivity {
    private DaysRecyclerView mDaysRecyclerView;
    private DayViewAdapter mDayAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Bootstrap Toolbar
        setContentView(R.layout.activity_crumbs);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bootstrap favicon
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Bootstrap DaysRecyclerView
        mDaysRecyclerView = findViewById(R.id.days_recycler);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        lm.setReverseLayout(true);
        mLayoutManager = lm;
        mDaysRecyclerView.setLayoutManager(mLayoutManager);

        JSONObject dataObject = new JSONObject();
        for (int i = 0; i < 10; i++) {
            try {
                dataObject.put("item" + i, new JSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mDayAdapter = new DayViewAdapter(dataObject);
        mDaysRecyclerView.setAdapter(mDayAdapter);
        ImageButton mPrevDayBtn = findViewById(R.id.previous_button);
        mPrevDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentDayItemPosition = ((LinearLayoutManager) mDaysRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                mDaysRecyclerView.smoothScrollToPosition(++currentDayItemPosition);
            }
        });
        final ImageButton mNextDayBtn = findViewById(R.id.forward_button);
        mNextDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentDayItemPosition = ((LinearLayoutManager) mDaysRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                int newPosition = mDaysRecyclerView.smoothScrollToPositionWithReturn(--currentDayItemPosition);
                if (newPosition > 0)
                    mNextDayBtn.setVisibility(View.VISIBLE);
                else
                    mNextDayBtn.setVisibility(View.GONE);
            }
        });

        mDaysRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE){
                    int currentDayItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    if (currentDayItemPosition == (mDayAdapter.getItemCount() - 1)) {
                        mDayAdapter.addDayViewItem(2);
                        mDayAdapter.notifyItemRangeInserted(mDayAdapter.getItemCount(), 2);
                        mDayAdapter.notifyDataSetChanged();
                    }
                    if (currentDayItemPosition > 0){
                        mNextDayBtn.setVisibility(View.VISIBLE);
                    }else{
                        mNextDayBtn.setVisibility(View.GONE);
                    }
                }
            }

           /* @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ProgressBar loadingData = findViewById(R.id.loading_data_progressBar);

            }*/
        });
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mDaysRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.day_view_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }
}
