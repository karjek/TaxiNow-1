package com.home.yassine.taxinow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facebook.FacebookSdk;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements ExternalAuthFragment.OnFragmentInteractionListener, IErrorText, IProgressShow {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */

    // UI references.
    private TextView mTextView;
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mLastNameView;
    private AutoCompleteTextView mFirstNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private final Activity context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mLastNameView = (AutoCompleteTextView) findViewById(R.id.lastName);
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.firstName);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mTextView = (TextView) findViewById(R.id.text_error_sign_up);

        mLoginFormView.requestFocus();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        HideKeyboard();

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String firstName = mFirstNameView.getText().toString();
        String genderString = null;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (lastName.length() < 3) {
            mLastNameView.setError(getString(R.string.error_invalid_lastName));
            focusView = mLastNameView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        } else if (firstName.length() < 3) {
            mFirstNameView.setError(getString(R.string.error_invalid_firstName));
            focusView = mFirstNameView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (password.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        else {
            RadioGroup genderGroup = (RadioGroup) findViewById(R.id.gender);
            AppCompatRadioButton gender;

            if (genderGroup != null)
            {
                int checkedId = genderGroup.getCheckedRadioButtonId();

                gender = (AppCompatRadioButton) findViewById(checkedId);

                if (gender != null) {
                    genderString = gender.getText().toString();
                } else {
                    AppCompatRadioButton radioButton = ((AppCompatRadioButton) findViewById(R.id.radio_f));
                    radioButton.setError(getString(R.string.select_one_of_both));
                    focusView = radioButton;
                    cancel = true;
                }
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("firstName", firstName);
                jsonObject.put("lastName", lastName);
                jsonObject.put("password", password);
                jsonObject.put("gender", String.valueOf((genderString.charAt(0))));
            } catch (JSONException e) {
                Log.e("e", e.getMessage());
            }
            StringEntity entity;
            try {
                entity = new StringEntity(jsonObject.toString());

                AsyncHttpClient client = new AsyncHttpClient();
                client.setMaxRetriesAndTimeout(1, 3000);
                client.post(this, HttpClient.BASE_URL+"auth/register", entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        Log.e("d", "START");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        try {

                            if (response != null) {
                                String jsonRes = new String(response, "UTF-8");

                                User u = User.FromJson(new JSONObject(jsonRes));

                                Storage.SaveUser(context, u);

                                startActivity(new Intent(context, MapsActivity.class));
                            }
                            else {
                                SetErrorText(getString(R.string.error_occured));
                            }

                        } catch (UnsupportedEncodingException | JSONException e1) {
                            SetErrorText(getString(R.string.error_occured));
                        }
                        showProgress(false);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

                        if (e instanceof ConnectException) {
                            SetErrorText(getString(R.string.err_connection));
                        }
                        else if (errorResponse == null) {
                            SetErrorText(getString(R.string.error_occured));
                        }
                        else {
                            try {

                                Log.e("d", new String(errorResponse, "UTF-8"));

                                JSONObject jsonObject1 = new JSONObject(new String(errorResponse, "UTF-8"));
                                SetErrorText(jsonObject1.getString("error"));

                            } catch (UnsupportedEncodingException | JSONException e1) {
                                SetErrorText(getString(R.string.error_occured));
                            }
                        }
                        showProgress(false);
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        Log.e("d", "RETRY");
                    }
                });

            } catch (UnsupportedEncodingException e) {
                showProgress(false);
                Log.e("e", e.getMessage());
                SetErrorText(getString(R.string.error_occured));
            }
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void HideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void SetErrorText(String error) {
        mTextView.setText(error);
        mTextView.setVisibility(View.VISIBLE);
        mTextView.requestFocus();
    }
}