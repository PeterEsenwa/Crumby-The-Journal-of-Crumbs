package ng.petersabs.dev.crumby;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_CONTACTS;
import static com.google.android.gms.auth.api.signin.GoogleSignIn.getClient;

/**
 * A login screen that offers login via email/password.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Ids to identify permission requests.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_INTERNET_ACCESS = 10;

    /**
     * A dummy authentication store containing known user names and passwords.
     * COMPLETED: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * TAG Variable for Logging Errors in the Google Sign up API
     */
    private static final String SIGNIN_ERR_TAG = "Error in Google Signin";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private GoogleUserLoginTask mGoogleAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private TextInputEditText mNewUsername;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private GoogleSignInClient mGoogleSignInClient;
    private int ReqCode_SIGNIN = 12;
    private int ReqCode_SIGNUP = 13;
    private String currentAction = "LOGIN";
    private TextInputEditText mNewUserEmail;
    private TextInputEditText mNewUserPassword;
    private TextInputEditText mNewUserRePassword;
    private Button mEmailSignUpButton;
    private TextView mGotoSignup;
    private createUserTask mSignUpTask;
    private createGoogleUserTask mGoogleSignUpTask;
    ScrollView mLoginForm;
    ScrollView mSignUpForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!(ContextCompat.checkSelfPermission(LoginActivity.this, INTERNET) == PackageManager.PERMISSION_GRANTED)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{INTERNET}, REQUEST_INTERNET_ACCESS);
                } else {
                    Toast.makeText(LoginActivity.this, R.string.prompt_for_perms_sdk_22_and_below, Toast.LENGTH_LONG).show();
                }
            }
        }
        // Switch forms when needed
        mLoginForm = findViewById(R.id.login_form_holder);
        mSignUpForm = findViewById(R.id.signup_form_holder);
        if (currentAction.equals("LOGIN")) {
            mLoginForm.setVisibility(View.VISIBLE);
            mSignUpForm.setVisibility(View.GONE);
        } else if (currentAction.equals("SIGNUP")) {
            mLoginForm.setVisibility(View.GONE);
            mSignUpForm.setVisibility(View.VISIBLE);
        }
        // Setup Signup Form
        mNewUsername = findViewById(R.id.new_user_name);
        mNewUserEmail = findViewById(R.id.new_user_email);
        mNewUserPassword = findViewById(R.id.new_user_password);
        mNewUserRePassword = findViewById(R.id.new_user_repassword);

        mEmailSignUpButton = findViewById(R.id.email_signup_button);
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_NULL) {
                    mPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        populateAutoComplete();
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        findViewById(R.id.back_to_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAction = "LOGIN";
                mSignUpForm.setVisibility(View.GONE);
                mLoginForm.setVisibility(View.VISIBLE);
            }
        });
        mGotoSignup = findViewById(R.id.goto_signup);
        mGotoSignup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAction = "SIGNUP";
                mLoginForm.setVisibility(View.GONE);
                mSignUpForm.setVisibility(View.VISIBLE);
            }
        });
        mLoginFormView = findViewById(R.id.email_login_form);
        mProgressView = findViewById(R.id.login_progress);
        findViewById(R.id.googleSignInButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleAuthTask != null) {
                    return;
                }
                mLoginForm.setVisibility(View.GONE);
                mSignUpForm.setVisibility(View.GONE);
                signIn();
            }
        });
        findViewById(R.id.googleSignUpButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleSignUpTask != null) {
                    return;
                }
                mLoginForm.setVisibility(View.GONE);
                mSignUpForm.setVisibility(View.GONE);
                signUp();
            }
        });

        // Google Signin config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .build();
        mGoogleSignInClient = getClient(this, gso);
    }

    private void attemptSignUp() {
        String newEmail = mNewUserEmail.getText().toString();
        String newUsername = mNewUsername.getText().toString();
        String newUserPassword = mNewUserPassword.getText().toString();

        // Refresh Error states
        mNewUserEmail.setError(null);
        mNewUsername.setError(null);
        mNewUserPassword.setError(null);

        boolean cancel = false;
        View focusView = null;


        if (!TextUtils.isEmpty(newUserPassword) && !isPasswordValid(newUserPassword)) {
            mNewUserPassword.setError(getString(R.string.error_invalid_password));
            focusView = mNewUserPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(newEmail)) {
            mNewUserEmail.setError(getString(R.string.error_username_required));
            focusView = mNewUserEmail;
            cancel = true;
        } else if (!isEmailValid(newEmail)) {
            mNewUserEmail.setError(getString(R.string.error_invalid_email));
            focusView = mNewUserEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            ScrollView mSignUpForm = findViewById(R.id.signup_form_holder);
            mSignUpForm.setVisibility(View.GONE);
            showProgress(true);
            mSignUpTask = new createUserTask(newUsername, newEmail, newUserPassword);
            mSignUpTask.execute((Void) null);
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        ScrollView mLoginForm = findViewById(R.id.login_form_holder);
//        ScrollView mSignUpForm = findViewById(R.id.signup_form_holder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ReqCode_SIGNIN) {
            if (resultCode == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            } else if (resultCode == RESULT_CANCELED) {
                mGoogleAuthTask = null;
                mLoginForm.setVisibility(View.VISIBLE);
            }
        }
        if (requestCode == ReqCode_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignUpResult(task);
            } else if (resultCode == RESULT_CANCELED) {
                mGoogleSignUpTask = null;
                mSignUpForm.setVisibility(View.VISIBLE);
            }
        }
    }

    private void handleSignUpResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            showProgress(true);
            mGoogleSignUpTask = new createGoogleUserTask(account.getDisplayName(), account.getEmail(), account.getId());
            mGoogleSignUpTask.execute((Void) null);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(SIGNIN_ERR_TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            showProgress(true);
            mGoogleAuthTask = new GoogleUserLoginTask(account.getEmail(), account.getId());
            mGoogleAuthTask.execute((Void) null);
            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(SIGNIN_ERR_TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    /**
     * Get signInIntent
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, ReqCode_SIGNIN);
    }

    private void signUp() {
        revokeAccess();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, ReqCode_SIGNUP);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_username_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            ScrollView mLoginForm = findViewById(R.id.login_form_holder);
            mLoginForm.setVisibility(View.GONE);
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private void attemptLogin(String Email) {
        Toast.makeText(LoginActivity.this, Email, Toast.LENGTH_LONG).show();
        revokeAccess();
    }

    private void revokeAccess() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        //COMPLETED: Replace this with your own logic
        return password.length() > 7;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public class createGoogleUserTask extends AsyncTask<Void, Void, String> {

        private final String mNewUserEmailString;
        private final String mNewUsernameString;
        private final String mNewUserIdString;

        private String Message;
        User checkUser;

        createGoogleUserTask(String mNewUsernameString, String mNewUserEmailString, String mNewUserIdString) {
            this.mNewUserEmailString = mNewUserEmailString;
            this.mNewUsernameString = mNewUsernameString;
            this.mNewUserIdString = mNewUserIdString;
        }

        @Override
        protected String doInBackground(Void... params) {
            UserDatabase database = UserDatabase.getDatabase(LoginActivity.this);
            UserDAO userDAO = database.userDAO();

            checkUser = userDAO.doesEmailExists(mNewUserEmailString);

            GoogleUserDB googleDB = GoogleUserDB.getINSTANCE(LoginActivity.this);
            GoogleUserDAO gUserDAO = googleDB.userDAO();

            GoogleUser newGUser = new GoogleUser(mNewUsernameString, mNewUserEmailString, mNewUserIdString);
            GoogleUser checkGoogleUser = gUserDAO.doesEmailExists(mNewUserEmailString);

           /* long status = userDAO.createNewUser(newUser);
            s = "Status: " + status;*/
            if (checkUser != null || checkGoogleUser != null) {
                Message = getString(R.string.email_already_exist);
            } else {
                Message = "Success";
                gUserDAO.newGoogleUser(newGUser);
            }
            return Message;
        }

        @Override
        protected void onPostExecute(final String Status) {
            mGoogleSignUpTask = null;
            showProgress(false);
            if (Objects.equals(Status, "Success")) {
                Intent intent = new Intent(LoginActivity.this, CrumbsActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (Objects.equals(Status, getString(R.string.email_already_exist))) {
                    Toast.makeText(LoginActivity.this, getString(R.string.email_already_exist), Toast.LENGTH_LONG).show();
                    ScrollView mSignUpForm = findViewById(R.id.signup_form_holder);
                    mSignUpForm.setVisibility(View.VISIBLE);
                    //  TODO: Auto signin user
                }
            }
        }

        @Override
        protected void onCancelled() {
            mGoogleSignUpTask = null;
            showProgress(false);
        }
    }

    public class createUserTask extends AsyncTask<Void, Void, String> {

        private final String mNewUserEmailString;
        private final String mNewUsernameString;
        private final String mNewUserPasswordString;

        private String Message;
        User checkUser;

        createUserTask(String mNewUsernameString, String mNewUserEmailString, String mNewUserPasswordString) {
            this.mNewUserEmailString = mNewUserEmailString;
            this.mNewUsernameString = mNewUsernameString;
            this.mNewUserPasswordString = mNewUserPasswordString;
        }

        @Override
        protected String doInBackground(Void... params) {
            UserDatabase database = UserDatabase.getDatabase(LoginActivity.this);
            UserDAO userDAO = database.userDAO();

            User newUser = new User(mNewUsernameString, mNewUserEmailString, mNewUserPasswordString);
            checkUser = userDAO.doesEmailExists(mNewUserEmailString);
            GoogleUserDB googleDB = GoogleUserDB.getINSTANCE(LoginActivity.this);
            GoogleUserDAO gUserDAO = googleDB.userDAO();

            GoogleUser checkGoogleUser = gUserDAO.doesEmailExists(mNewUserEmailString);


            if (checkUser != null || checkGoogleUser != null)
                Message = getString(R.string.email_already_exist);
            else {
                checkUser = userDAO.doesNameExists(mNewUsernameString);
                if (checkUser != null)
                    Message = getString(R.string.username_already_exist);
                else {
                    Message = "Success";
                    userDAO.createNewUser(newUser);
                }
            }
            return Message;
        }

        @Override
        protected void onPostExecute(final String Status) {
            showProgress(false);
            if (Objects.equals(Status, "Success")) {
                Intent intent = new Intent(LoginActivity.this, CrumbsActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (Objects.equals(Status, getString(R.string.email_already_exist))) {
                    ScrollView mSignUpForm = findViewById(R.id.signup_form_holder);
                    mSignUpForm.setVisibility(View.VISIBLE);
                    mNewUserEmail.setError(getString(R.string.email_already_exist));
                    mNewUserEmail.requestFocus();
                } else if (Objects.equals(Status, getString(R.string.username_already_exist))) {
                    ScrollView mSignUpForm = findViewById(R.id.signup_form_holder);
                    mSignUpForm.setVisibility(View.VISIBLE);
                    mNewUsername.setError(getString(R.string.username_already_exist));
                    mNewUsername.requestFocus();
                }

            }
//
        }
    }

    public class GoogleUserLoginTask extends AsyncTask<Void, Void, String> {
        private final String mEmail;
        private final String mGoogleID;
        private String Message;

        public GoogleUserLoginTask(String mEmail, String mGoogleID) {
            this.mEmail = mEmail;
            this.mGoogleID = mGoogleID;
        }

        @Override
        protected String doInBackground(Void... voids) {
            GoogleUserDB googleDB = GoogleUserDB.getINSTANCE(LoginActivity.this);
            GoogleUserDAO gUserDAO = googleDB.userDAO();

//            GoogleUser User = new GoogleUser(mNewUsernameString, mNewUserEmailString, mNewUserIdString);
            GoogleUser checkGoogleUser = gUserDAO.doesEmailExists(mEmail);

           /* long status = userDAO.createNewUser(newUser);
            s = "Status: " + status;*/
            if (checkGoogleUser != null) {
                Message = "Success";
            } else {
                Message = getString(R.string.account_doesnt_exist);
            }
            return Message;
        }

        @Override
        protected void onPostExecute(final String Status) {
            mGoogleAuthTask = null;
            showProgress(false);
            mLoginForm.setVisibility(View.VISIBLE);

            if (Status.equals("Success")) {
                Toast.makeText(LoginActivity.this, "Login Succesful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, CrumbsActivity.class);
                startActivity(intent);
                finish();
            } else {
                revokeAccess();
                Toast.makeText(LoginActivity.this, Status, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mGoogleAuthTask = null;
            showProgress(false);
            mLoginForm.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, ng.petersabs.dev.crumby.User> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected ng.petersabs.dev.crumby.User doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            UserDatabase database = UserDatabase.getDatabase(LoginActivity.this);
            UserDAO userDAO = database.userDAO();

            User user = userDAO.getUserForLogin(mEmail, mPassword);


            if (user != null) {
                return user;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final User User) {
            mAuthTask = null;
            ScrollView mLoginForm = findViewById(R.id.login_form_holder);
            mLoginForm.setVisibility(View.VISIBLE);
            showProgress(false);

            if (User != null) {
                Intent intent = new Intent(LoginActivity.this, CrumbsActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.error_incorrect_password), Toast.LENGTH_LONG).show();

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

