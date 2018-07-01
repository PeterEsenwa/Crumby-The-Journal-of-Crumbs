package ng.petersabs.dev.crumby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class CrumbsActivity extends AppCompatActivity {
    private DaysRecyclerView mDaysRecyclerView;
    private DayViewAdapter mDayAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    final DateFormat dateFormat =
            new SimpleDateFormat("dd MMMM", Locale.getDefault());
    FirebaseFirestore db;
    String currentDateFormatted;
    private SharedPreferences sp;
    private Context context;
    private ArrayList crumbs = new ArrayList();
    private int noOfCrumbs;
    private String currentUsername;
    private int mDayInViewOnLeave = 0;
    private Bundle outState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get user details from preferences
        context = CrumbsActivity.this;
        sp = getDefaultSharedPreferences(context);
        currentUsername = sp.getString(getString(R.string.active_username), "none");
        if (currentUsername.equalsIgnoreCase("none")) {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        // Setup Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);


        //Bootstrap Toolbar
        setContentView(R.layout.activity_crumbs);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bootstrap add new FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int mCurrentAdapterPosition = ((LinearLayoutManager) mDaysRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                calendar.add(Calendar.DATE, 0 - mCurrentAdapterPosition);
                Date curDate = calendar.getTime();
                currentDateFormatted = dateFormat.format(curDate);
                Intent addOrEditIntent = new Intent(context, AddOrEditCrumb.class);
                addOrEditIntent.putExtra("current_date", currentDateFormatted);
                addOrEditIntent.putExtra("title", getString(R.string.tag_for_new_crumb));
                addOrEditIntent.putExtra("crumb_text", getString(R.string.tag_for_new_crumb));
                ((Activity) context).startActivityForResult(addOrEditIntent, 20);

            }
        });

        //Bootstrap DaysRecyclerView
        mDaysRecyclerView = findViewById(R.id.days_recycler);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        lm.setReverseLayout(true);
        mLayoutManager = lm;
        mDaysRecyclerView.setLayoutManager(mLayoutManager);

        mDayAdapter = new DayViewAdapter(new ArrayList<HashMap>(), CrumbsActivity.this);
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

        // TODO Add goto date

        mDaysRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    int currentDayItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    if (currentDayItemPosition == (mDayAdapter.getItemCount() - 1)) {
                        mDayAdapter.addDayViewItem(2);
                        mDayAdapter.notifyItemRangeInserted(mDayAdapter.getItemCount(), 2);
                        mDayAdapter.notifyDataSetChanged();
                    }
                    if (currentDayItemPosition > 0) {
                        mNextDayBtn.setVisibility(View.VISIBLE);
                    } else {
                        mNextDayBtn.setVisibility(View.GONE);
                    }
                }
            }

        });
        new PagerSnapHelper().attachToRecyclerView(mDaysRecyclerView);
        if (savedInstanceState != null) {
            mDayInViewOnLeave = savedInstanceState.getInt("mDayViewOnLeave", 0);
            mDaysRecyclerView.smoothScrollToPosition(mDayInViewOnLeave);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (outState != null) {
            mDayInViewOnLeave = outState.getInt("mDayViewOnLeave", 0);
        }
        mDaysRecyclerView.smoothScrollToPosition(mDayInViewOnLeave);
        String user_name = sp.getString(getString(R.string.active_username), "none");
        String user_email = sp.getString(getString(R.string.active_useremail), "none");
        DocumentReference currentUserData = db.collection("google_users_data").document(user_email);
        currentUserData.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot userSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Bad Firestore Listen", "Listen failed.", e);
                }
                if (userSnapshot != null && userSnapshot.exists()) {
                    Map<String, Object> map = userSnapshot.getData();
                    if (map != null) {
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            if (entry.getKey().equals("crumbs")) {
                                crumbs = (ArrayList) entry.getValue();
                                if (crumbs != null && crumbs.size() > 0) {
                                    mDayAdapter.updateData(crumbs);
                                }
                            }
                        }
                    }
                } else {
                    crumbs = new ArrayList();
                }
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (outState != null) {
            mDayInViewOnLeave = savedInstanceState.getInt("mDayViewOnLeave", 0);
        }
        mDaysRecyclerView.smoothScrollToPosition(mDayInViewOnLeave);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        this.outState = outState;
        int currentDayItemPosition = ((LinearLayoutManager) mDaysRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        outState.putInt("mDayViewOnLeave", currentDayItemPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings_item) {
            Intent settingsIntent = new Intent(CrumbsActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.day_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode == RESULT_OK) {
            Calendar calendar = Calendar.getInstance();
            int mCurrentAdapterPosition = ((LinearLayoutManager) mDaysRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            calendar.add(Calendar.DATE, 0 - mCurrentAdapterPosition);
            final Date curDate = calendar.getTime();
            String user_email = sp.getString(getString(R.string.active_useremail), "none");
            DocumentReference currentUserData = db.collection("google_users_data").document(user_email);
            HashMap crumb = new HashMap();
            String title = data.getStringExtra("new_title");
            String crumb_text = data.getStringExtra("new_crumb_text");
            if (title != null && crumb_text != null && !title.equals("")) {
                crumb.put("date", curDate);
                crumb.put("title", title);
                crumb.put("crumb", crumb_text);
                crumbs.add(crumb);
            }
            currentUserData.update("crumbs", crumbs);
        }
    }
}
