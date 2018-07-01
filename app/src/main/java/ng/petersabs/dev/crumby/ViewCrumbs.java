package ng.petersabs.dev.crumby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Objects;

import javax.annotation.Nullable;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class ViewCrumbs extends AppCompatActivity {
    final DateFormat dateFormat =
            new SimpleDateFormat("dd MMMM", Locale.getDefault());
    FirebaseFirestore db;
    ArrayList<HashMap> crumbs;
    String mUserEmail;
    ArrayList<HashMap> updateData;
    DocumentReference usersData;
    ArrayList<HashMap> mCrumbsForToday;
    private FloatingActionButton fab;
    private RecyclerView mCrumbsRecyclerView;
    private ViewCrumbsAdapter mCrumbsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedPreferences sp;
    private Context context;
    private String mCurrentUsername;
    private int mCurrentPosition;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_crumbs);
        context = ViewCrumbs.this;
        sp = getDefaultSharedPreferences(context);
        mCurrentUsername = sp.getString(getString(R.string.active_username), "none");
        mUserEmail = sp.getString(getString(R.string.active_useremail), "none");
        if (mCurrentUsername.equalsIgnoreCase("none")) {
            Intent loginIntent = new Intent(context, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        Intent intent = getIntent();
        mCrumbsForToday = new ArrayList<>();
        String mCurrentDateString = "";
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        if (intent != null && intent.getStringExtra("current_day") != null) {
            mCrumbsForToday = (ArrayList<HashMap>) Objects.requireNonNull(intent.getExtras()).getSerializable("all_crumbs_today");
            mCurrentDateString = intent.getStringExtra("current_day");
            mCurrentPosition = intent.getIntExtra("position", 0);
        }

        setSupportActionBar(toolbar);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mCrumbsRecyclerView = findViewById(R.id.crumbs_recycler);
        mCrumbsRecyclerView.setLayoutManager(mLayoutManager);
        mCrumbsAdapter = new ViewCrumbsAdapter(mCrumbsForToday, mCurrentDateString, ViewCrumbs.this);
        mCrumbsRecyclerView.setAdapter(mCrumbsAdapter);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 0 - mCurrentPosition);
                Date curDate = calendar.getTime();
                String currentDateFormatted = dateFormat.format(curDate);
                Intent addOrEditIntent = new Intent(context, AddOrEditCrumb.class);
                addOrEditIntent.putExtra("current_date", currentDateFormatted);
                addOrEditIntent.putExtra("title", getString(R.string.tag_for_new_crumb));
                addOrEditIntent.putExtra("crumb_text", getString(R.string.tag_for_new_crumb));
                ((Activity) context).startActivityForResult(addOrEditIntent, 21);
            }
        });
    }

    ArrayList<HashMap> getCrumbsForDate(Date mDayViewDate, ArrayList<HashMap> Crumbs) {
        ArrayList<HashMap> mCrumbsOnDate = new ArrayList<>();
        for (HashMap crumb : Crumbs) {
            Date mCurrentCrumbDate = (Date) crumb.get("date");
            if (mCurrentCrumbDate != null && mDayViewDate.getDate() == mCurrentCrumbDate.getDate()) {
                mCrumbsOnDate.add(crumb);
            }
        }
        return mCrumbsOnDate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_crumbs_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) searchItem.getActionView();
        sv.setQueryHint("Find crumbs by title...");
        fab = findViewById(R.id.fab);
        MenuItem.OnActionExpandListener expandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                fab.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                fab.setVisibility(View.GONE);
                return true;
            }
        };
        searchItem.setOnActionExpandListener(expandListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(ViewCrumbs.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() !=  null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        usersData = db.collection("google_users_data").document(mUserEmail);
        usersData.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.add(Calendar.DATE, 0 - mCurrentPosition);
                                    Date curDate = calendar.getTime();
                                    updateData = getCrumbsForDate(curDate, crumbs);
                                    mCrumbsAdapter.updateData(updateData);
                                }
                            }
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 22 && resultCode == RESULT_OK) {
//            mCrumbsForToday
            usersData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Map<String, Object> map = task.getResult().getData();
                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                if (entry.getKey().equals("crumbs")) {
                                    crumbs = (ArrayList) entry.getValue();

                                    if (crumbs != null && crumbs.size() > 0) {
                                        int mCrumbPositon = 0;
                                        for (HashMap crumb : crumbs) {
                                            String mCrumbTitle = (String) crumb.get("title");
                                            String mCrumbText = (String) crumb.get("crumb");
                                            String mOldTitle = data.getStringExtra("title");
                                            String mChangedTitle = data.getStringExtra("new_title");
                                            String mChangedText = data.getStringExtra("new_crumb_text");
                                            String mOldText = data.getStringExtra("crumb_text");
                                            if (mCrumbTitle.equalsIgnoreCase(mOldTitle) && mCrumbText.equalsIgnoreCase(mOldText)) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    crumb.replace("crumb", mOldText, mChangedText);
                                                    crumb.replace("title", mOldTitle, mChangedTitle);
                                                } else {
                                                    crumb.remove("title");
                                                    crumb.remove("crumb");
                                                    crumb.put("title", mChangedTitle);
                                                    crumb.put("crumb", mChangedText);
                                                }
                                                crumbs.set(mCrumbPositon, crumb);
                                                usersData.update("crumbs", crumbs);
                                                break;
                                            }
                                            mCrumbPositon += 1;
                                        }
                                    }
                                }
                            }
                        }
                        /*for (DocumentSnapshot document : task.getResult()) {
                            Log.d("Lets See", document.getId() + " => " + document.getData());
                        }*/
                    } else {
                        Log.d("Firestore Error", "Error getting documents: ", task.getException());
                    }
                }

            });
        } else if (requestCode == 21 && resultCode == RESULT_OK) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 0 - mCurrentPosition);
            Date curDate = calendar.getTime();
            HashMap crumb = new HashMap();
            String title = data.getStringExtra("new_title");
            String crumb_text = data.getStringExtra("new_crumb_text");
            if (title != null && crumb_text != null && !title.equals("")) {
                crumb.put("date", curDate);
                crumb.put("title", title);
                crumb.put("crumb", crumb_text);
                crumbs.add(crumb);
            }
            usersData.update("crumbs", crumbs);
        }
    }

    Boolean deleteCrumb(int position) {
        int index = 0;
        HashMap crumbAtPosition = updateData.get(position);
        for (HashMap crumb : crumbs) {
            String mCrumbTitle = (String) crumb.get("title");
            String mCrumbText = (String) crumb.get("crumb");
            String mOldTitle = (String) crumbAtPosition.get("title");
            String mOldText = (String) crumbAtPosition.get("crumb");
            if (mCrumbTitle.equalsIgnoreCase(mOldTitle) && mCrumbText.equalsIgnoreCase(mOldText)) {
                crumbs.remove(index);
                usersData.update("crumbs", crumbs);
                break;
            }
            index += 1;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
