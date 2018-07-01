package ng.petersabs.dev.crumby;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_visualizer);
        SharedPreferences sp = getDefaultSharedPreferences(getContext());
        String mUserEmail = sp.getString(getString(R.string.active_useremail), "none");
        String mUserName = sp.getString(getString(R.string.active_username), "none");
        String mUserPassword = sp.getString(getString(R.string.active_userpassword), "none");
        String mUserID = sp.getString(getString(R.string.active_userid), "none");
        if (mUserEmail.equalsIgnoreCase("Default value")) {
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        } else {
            findPreference(getString(R.string.active_username)).setSummary(mUserName);
            EditTextPreference useremailPref = (EditTextPreference) findPreference(getString(R.string.active_useremail));
            useremailPref.setSummary(mUserEmail);
            if (mUserPassword.equalsIgnoreCase("Default value")) {
                Preference passwordPref = findPreference(getString(R.string.active_userpassword));
                getPreferenceScreen().removePreference(passwordPref);
            } else {
                findPreference(getString(R.string.active_userpassword)).setSummary(mUserPassword);
            }

            if (mUserID.equalsIgnoreCase("Default value")) {
                getPreferenceScreen().removePreference(findPreference(getString(R.string.active_userid)));
            } else {
                findPreference(getString(R.string.active_userid)).setSummary(mUserID);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        final UserDatabase database = UserDatabase.getDatabase(getContext());
        final UserDAO userDAO = database.userDAO();

        final GoogleUserDB googleDB = GoogleUserDB.getINSTANCE(getContext());
        final GoogleUserDAO gUserDAO = googleDB.userDAO();
        SharedPreferences sp = getDefaultSharedPreferences(getContext());
        final String mUserEmail = sp.getString(getString(R.string.active_useremail), "none");
        final String newValue = sp.getString(key, "none");
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                findPreference(key).setSummary(newValue);
                User checkUser = userDAO.doesEmailExists(mUserEmail);
                GoogleUser checkGoogleUser = gUserDAO.doesEmailExists(mUserEmail);

                if (checkUser != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .build();
                    db.setFirestoreSettings(settings);
                    String passwordPrefKey = getString(R.string.active_userpassword);
                    String usernamePrefKey = getString(R.string.active_username);
                    if (key.equalsIgnoreCase(passwordPrefKey)) {
                        String newPassword = sharedPreferences.getString(passwordPrefKey, "none");
                        checkUser.setPassword(newPassword);
                        db.collection("google_users_data").document(checkUser.getUserEmail()).update("password", newPassword);
                    }
                    if (key.equalsIgnoreCase(usernamePrefKey)) {
                        String userName = sharedPreferences.getString(usernamePrefKey, "none");
                        checkUser.setUserName(userName);
                        db.collection("google_users_data").document(checkUser.getUserEmail()).update("username", userName);
                    }
                    userDAO.updateUserDetails(checkUser);
                }
                if (checkGoogleUser != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .build();
                    db.setFirestoreSettings(settings);
                    String usernamePrefKey = getString(R.string.active_username);
                    if (key.equalsIgnoreCase(usernamePrefKey)) {
                        String userName = sharedPreferences.getString(usernamePrefKey, "none");
                        checkGoogleUser.setUserName(userName);
                        db.collection("google_users_data").document(checkGoogleUser.getUserEmail()).update("username", userName);
                    }
                    gUserDAO.updateUserDetails(checkGoogleUser);
                }
                return null;
            }
        }.execute((Void) null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
