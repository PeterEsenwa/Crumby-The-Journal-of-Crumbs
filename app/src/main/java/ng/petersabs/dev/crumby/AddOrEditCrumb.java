package ng.petersabs.dev.crumby;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

public class AddOrEditCrumb extends AppCompatActivity {
    TextView mDateDisplay;
    TextView mCrumbTitleTextView;
    FirebaseFirestore db;
    TextView mCrumbDetailsTextView;
    String mCurrentDate;
    Intent intent;
    String mCrumbTitle = "";
    String mCrumbDetails = "";
    Bundle conData = new Bundle();
    Button mSaveButton;
    Button mDiscardButton;
    Boolean mClosingProperly = false;
    private SharedPreferences sp;
    private Context context;
    private String mCurrentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_or_edit_crumb);
        intent = getIntent();
        mDateDisplay = findViewById(R.id.on_day_textview);
        mCrumbTitleTextView = findViewById(R.id.crumb_title_textview);
        mCrumbDetailsTextView = findViewById(R.id.crumb_details_textview);
        mSaveButton = findViewById(R.id.save_crumb);
        context = this;
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData("new_title", mCrumbTitleTextView.getText().toString());
                setData("new_crumb_text", mCrumbDetailsTextView.getText().toString());
                setResult(RESULT_OK, intent);
                mClosingProperly = true;
                finish();
            }
        });
        mDiscardButton = findViewById(R.id.discard_crumb);
        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData("new_title", "");
                setData("new_crumb_text", "");
                setResult(RESULT_CANCELED, intent);
                mClosingProperly = true;
                finish();
            }
        });
        if (intent != null && intent.getStringExtra("current_date") != null) {
            mCurrentDate = intent.getStringExtra("current_date");
            mCrumbTitle = intent.getStringExtra("title");
            mCrumbDetails = intent.getStringExtra("crumb_text");
        }

        if (!mCurrentDate.equals("")) {
            if (!mCrumbTitle.equalsIgnoreCase(getString(R.string.tag_for_new_crumb)) && !mCrumbDetails.equalsIgnoreCase(getString(R.string.tag_for_new_crumb))) {
                mCurrentDate = mCurrentDate + " - Edit Crumb";
                mDateDisplay.setText(mCurrentDate);
                mCrumbTitleTextView.setText(mCrumbTitle);
                mCrumbDetailsTextView.setText(mCrumbDetails);
            } else {
                mCurrentDate = mCurrentDate + " - New Crumb";
                mDateDisplay.setText(mCurrentDate);
                mCrumbTitleTextView.setText("");
                mCrumbDetailsTextView.setText("");
            }
        }
    }

    @Override
    protected void onStop() {
        if (!mClosingProperly)
            setResult(RESULT_CANCELED, intent);
        finish();
        super.onStop();
    }

    private void setData(String key, String value) {
        intent.putExtra(key, value);
    }
}
