package com.home.yassine.taxinow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExternalAuthFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExternalAuthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExternalAuthFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private OnFragmentInteractionListener mListener;
    private Context hostActivity;
    private CustomLoginButton mFacebookLogin;
    private SignInButton mGoogleLogin;
    private GoogleApiClient mGoogleApiClient; // google
    private CallbackManager callbackMgr; // fb
    private String mGender;
    private String mToken;

    public ExternalAuthFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExternalAuthFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExternalAuthFragment newInstance() {
        ExternalAuthFragment fragment = new ExternalAuthFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                if (account != null)
                {
                    mToken = account.getServerAuthCode();
                }

                if (mGender == null) {
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
                }
                else {
                    Connect(mToken, ExternalLoginProvider.GOOGLE);
                }

            } else {
                // Google Sign In failed, update UI appropriately
                Log.d("M", result.getStatus().toString());
            }
        }
        else {
            callbackMgr.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mFacebookLogin = (CustomLoginButton) getView().findViewById(R.id.fb_login_btn);
        mGoogleLogin = (SignInButton) getView().findViewById(R.id.google_login_btn);

        if (hostActivity instanceof SignInActivity) {
            setGooglePlusButtonText(mGoogleLogin, getString(R.string.sign_in_with_google));
            mFacebookLogin.mCustomText = getString(R.string.sign_in_with_facebook);
        }
        else {
            setGooglePlusButtonText(mGoogleLogin, getString(R.string.sign_up_with_google));
            mFacebookLogin.mCustomText = getString(R.string.sign_up_with_facebook);
        }

        callbackMgr = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getString(R.string.google_client_id))
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope("https://www.googleapis.com/auth/plus.login"))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(hostActivity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addApi(Plus.API)
                .build();

        mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);

        mGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, 9001);
            }
        });

        mFacebookLogin.setReadPermissions("email");
        mFacebookLogin.setFragment(this);
        mFacebookLogin.registerCallback(callbackMgr, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                Connect(token.getToken(), ExternalLoginProvider.FACEBOOK);
            }

            @Override
            public void onCancel() {
                Log.e("err", "canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("err", "connection failed");
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {

            if (mGoogleApiClient.hasConnectedApi(Plus.API)) {

                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

                if (person != null) {
                    mGender = person.getGender() == 0 ? "G" : "F";
                    Log.e("GENDER", String.valueOf(person.getGender()));
                }

                if (mToken != null)
                    Connect(mToken, ExternalLoginProvider.GOOGLE);
            }
            else {
                Toast.makeText(hostActivity, getString(R.string.err_connection), Toast.LENGTH_SHORT);
            }

        }catch (Exception e) {
            Log.e("EROOOOR", e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    enum ExternalLoginProvider {
        FACEBOOK,
        GOOGLE
    }

    private void Connect(final String token, ExternalLoginProvider provider) {

        final IErrorText hostActivityError = (IErrorText) hostActivity;
        final IProgressShow hostActivityProgress = (IProgressShow) hostActivity;

        hostActivityProgress.showProgress(true);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", token);
        } catch (JSONException e) {
            Log.e("e", e.getMessage());
            hostActivityError.SetErrorText(hostActivity.getString(R.string.error_occured));
            hostActivityProgress.showProgress(false);
        }
        StringEntity entity;
        try {
            entity = new StringEntity(jsonObject.toString());

            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(1, 3000);

            String url = HttpClient.BASE_URL+"auth/";

            if (provider == ExternalLoginProvider.FACEBOOK)
                url += "facebook";
            else
                url += mGender == null ? "google" : "google/"+mGender;

            client.post(hostActivity, url, entity, "application/json", new AsyncHttpResponseHandler() {
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

                            Storage.SaveUser((Activity)hostActivity, u);

                            startActivity(new Intent(hostActivity, MapsActivity.class));
                        }
                        else {
                            hostActivityError.SetErrorText(hostActivity.getString(R.string.error_occured));
                        }

                    } catch (UnsupportedEncodingException | JSONException e1) {
                        hostActivityError.SetErrorText(hostActivity.getString(R.string.error_occured));
                    }
                    hostActivityProgress.showProgress(false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

                    if (e instanceof ConnectException) {
                        hostActivityError.SetErrorText(hostActivity.getString(R.string.err_connection));
                    }
                    else if (errorResponse == null) {
                        hostActivityError.SetErrorText(hostActivity.getString(R.string.error_occured));
                    }
                    else {
                        try {

                            Log.e("d", new String(errorResponse, "UTF-8"));

                            JSONObject jsonObject1 = new JSONObject(new String(errorResponse, "UTF-8"));
                            hostActivityError.SetErrorText(jsonObject1.getString("error"));

                        } catch (UnsupportedEncodingException | JSONException e1) {
                            hostActivityError.SetErrorText(hostActivity.getString(R.string.error_occured));
                        }
                    }
                    hostActivityProgress.showProgress(false);
                }

                @Override
                public void onRetry(int retryNo) {
                    Log.e("d", "RETRY");
                }
            });
        } catch (UnsupportedEncodingException e) {
            Log.e("e", e.getMessage());
            hostActivityError.SetErrorText(hostActivity.getString(R.string.error_occured));
            hostActivityProgress.showProgress(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_external_auth, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        hostActivity = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setTextSize(15);
                tv.setPadding(0, 0, 0, 0);
                return;
            }
        }
    }
}

