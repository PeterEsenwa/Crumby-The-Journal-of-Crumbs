package ng.petersabs.dev.crumby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.android.gms.auth.api.signin.GoogleSignIn.getClient;

public class SettingsActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .build();
        mGoogleSignInClient = getClient(this, gso);
        final SharedPreferences sp = getDefaultSharedPreferences(this);
        final String mUserPassword = sp.getString(getString(R.string.active_userpassword), "none");
        final String mUserID = sp.getString(getString(R.string.active_userid), "none");

        findViewById(R.id.logout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().remove(getString(R.string.active_useremail)).apply();
                sp.edit().remove(getString(R.string.active_username)).apply();
                if (mUserPassword.equalsIgnoreCase("Default value") || mUserPassword.equalsIgnoreCase("none")) {
                    sp.edit().remove(getString(R.string.active_userpassword)).apply();
                }
                if (mUserID.equalsIgnoreCase("Default value") || mUserID.equalsIgnoreCase("none")) {
                    sp.edit().remove(getString(R.string.active_userid)).apply();
                }
                revokeAccess();
            }
        });
    }

    private void revokeAccess() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent LoginIntent = new Intent(SettingsActivity.this, LoginActivity.class);
                        startActivity(LoginIntent);
                        finish();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
