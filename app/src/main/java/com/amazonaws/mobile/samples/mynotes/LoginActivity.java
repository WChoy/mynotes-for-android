package com.amazonaws.mobile.samples.mynotes;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInActivity;
import com.amazonaws.mobile.samples.mynotes.R;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsClient;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.pinpoint.targeting.endpointProfile.EndpointProfile;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final IdentityManager identityManager = AWSProvider.getInstance().getIdentityManager();
        final PinpointManager pinpointManager = AWSProvider.getInstance().getPinpointManager();
        final AnalyticsClient mgr = pinpointManager.getAnalyticsClient();

        // Set up the callbacks to handle the authentication response
        identityManager.setUpToAuthenticate(this, new DefaultSignInResultHandler() {
            @Override
            public void onSuccess(Activity activity, IdentityProvider identityProvider) {
                // Update the Endpoint Profile to include user ID
                final EndpointProfile profile = pinpointManager.getTargetingClient().currentEndpoint();
                profile.getUser().setUserId(identityManager.getCachedUserID());
                pinpointManager.getTargetingClient().updateEndpointProfile(profile);

                // Record the user login
                final AnalyticsEvent event = mgr.createEvent("_userauth.sign_in");
                mgr.recordEvent(event);
                mgr.submitEvents();

                Toast.makeText(LoginActivity.this,
                        String.format("Logged in as %s", identityManager.getCachedUserID()),
                        Toast.LENGTH_LONG).show();
                // Go to the main activity
                final Intent intent = new Intent(activity, NoteListActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                activity.finish();
            }

            @Override
            public boolean onCancel(Activity activity) {
                return false;
            }
        });

        // Start the authentication UI
        AuthUIConfiguration config = new AuthUIConfiguration.Builder()
                .userPools(true)
                .build();
        SignInActivity.startSignInActivity(this, config);
        LoginActivity.this.finish();
    }
}
