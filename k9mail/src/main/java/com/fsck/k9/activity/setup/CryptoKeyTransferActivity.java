package com.fsck.k9.activity.setup;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;
import com.fsck.k9.ui.crypto.backup.CryptoKeyTransferPresenter;


/**
 * Prompts the user for the email address and password.
 * Attempts to lookup default settings for the domain the user specified. If the
 * domain is known the settings are handed off to the AccountSetupCheckSettings
 * activity. If no settings are found the settings are handed off to the
 * AccountSetupAccountType activity.
 */
public class CryptoKeyTransferActivity extends K9Activity {
    public static final String EXTRA_ACCOUNT = "account";

    public static final int VIEW_INDEX_OVERVIEW = 0;
    public static final int REQUEST_MASK_PRESENTER = (1 << 8);


    private CryptoKeyTransferPresenter presenter;
    private ViewAnimator statusViewAnimator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crypto_key_transfer);

        statusViewAnimator = (ViewAnimator) findViewById(R.id.backup_status_animator);

        findViewById(R.id.transfer_receive_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickTransferReceive();
            }
        });

        findViewById(R.id.transfer_send_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickTransferSend();
            }
        });

        presenter = new CryptoKeyTransferPresenter(getApplicationContext(), this);
        presenter.initFromIntent(getIntent());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void setStatusToOverview() {
        statusViewAnimator.setDisplayedChild(VIEW_INDEX_OVERVIEW);
    }

    public void closeWithInvalidAccountError() {
        Toast.makeText(this, "Account not found!", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode & REQUEST_MASK_PRESENTER) == REQUEST_MASK_PRESENTER) {
            requestCode ^= REQUEST_MASK_PRESENTER;
            presenter.onActivityResult(requestCode, resultCode, data);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void launchUserInteractionPendingIntent(PendingIntent pendingIntent, int requestCode) {
        requestCode |= REQUEST_MASK_PRESENTER;
        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, null, 0, 0, 0);
        } catch (SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        presenter = null;
        super.onDestroy();
    }

    public void setStatusToError() {
        Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
    }

    public void setStatusToPending() {

    }
}
