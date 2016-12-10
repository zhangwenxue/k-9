
package com.fsck.k9.activity.setup;


import java.util.List;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;
import com.fsck.k9.mailstore.LocalMessage;
import com.fsck.k9.ui.crypto.backup.CryptoBackupMessageAdapter;
import com.fsck.k9.ui.crypto.backup.CryptoManageBackupPresenter;


/**
 * Prompts the user for the email address and password.
 * Attempts to lookup default settings for the domain the user specified. If the
 * domain is known the settings are handed off to the AccountSetupCheckSettings
 * activity. If no settings are found the settings are handed off to the
 * AccountSetupAccountType activity.
 */
public class CryptoManageBackup extends K9Activity {
    public static final String EXTRA_ACCOUNT = "account";

    public static final int VIEW_INDEX_PROGRESS = 0;
    public static final int VIEW_INDEX_UNSUPPORTED = 1;
    public static final int VIEW_INDEX_ENABLED_EMPTY = 2;
    public static final int VIEW_INDEX_ENABLED_OK = 3;
    public static final int REQUEST_MASK_PRESENTER = (1 << 8);


    private CryptoManageBackupPresenter presenter;
    private CryptoBackupMessageAdapter adapter;
    private ViewAnimator statusViewAnimator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crypto_manage_backup);

        statusViewAnimator = (ViewAnimator) findViewById(R.id.backup_status_animator);

        findViewById(R.id.backup_none_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickBackupNow();
            }
        });

        findViewById(R.id.backup_list_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickBackupNow();
            }
        });

        ListView listView = (ListView) findViewById(R.id.crypto_backup_list);

        presenter = new CryptoManageBackupPresenter(getApplicationContext(), this);
        presenter.initFromIntent(getIntent());

        adapter = new CryptoBackupMessageAdapter(this);
        listView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void setStatusToProgress() {
        statusViewAnimator.setDisplayedChild(VIEW_INDEX_PROGRESS);
    }

    public void setStatusToUnsupported() {
        statusViewAnimator.setDisplayedChild(VIEW_INDEX_UNSUPPORTED);
    }

    public void setStatusToError() {
        Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
    }

    public void setStatusToEnabledEmpty() {
        statusViewAnimator.setDisplayedChild(VIEW_INDEX_ENABLED_EMPTY);
    }

    public void setStatusToBackupOk(List<LocalMessage> messages) {
        statusViewAnimator.setDisplayedChild(VIEW_INDEX_ENABLED_OK);

        adapter.clear();
        adapter.addAll(messages);
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
}
