package com.fsck.k9.ui.crypto.backup;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.fsck.k9.Account;
import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.activity.setup.CryptoKeyTransferActivity;
import com.fsck.k9.mailstore.interactor.CryptoBackupInteractor;
import com.fsck.k9.mailstore.interactor.CryptoBackupInteractor.BackupStatus;
import com.fsck.k9.mailstore.interactor.CryptoBackupInteractor.BackupStatusType;
import org.openintents.openpgp.util.OpenPgpSimpleApi;
import org.openintents.openpgp.util.OpenPgpSimpleApi.BackupDataResult;
import org.openintents.openpgp.util.OpenPgpSimpleApi.InteractionRequestOrResult;


public class CryptoKeyTransferPresenter {
    private static final int REQUEST_CODE_BACKUP = 1;


    private Context context;
    private CryptoKeyTransferActivity view;
    private CryptoBackupInteractor cryptoBackupInteractor;

    private BackupStatus currentBackupStatus;
    private OpenPgpSimpleApi openPgpSimpleApi;
    private Account account;


    public CryptoKeyTransferPresenter(Context context, CryptoKeyTransferActivity view) {
        this.context = context;
        this.view = view;
    }

    public void initFromIntent(Intent intent) {
        String accountUuid = intent.getStringExtra(CryptoKeyTransferActivity.EXTRA_ACCOUNT);
        if (accountUuid == null) {
            view.closeWithInvalidAccountError();
            return;
        }

        account = Preferences.getPreferences(context).getAccount(accountUuid);
        cryptoBackupInteractor = CryptoBackupInteractor.getInstance(account);
        openPgpSimpleApi = OpenPgpSimpleApi.getInstance(context, K9.getOpenPgpProvider());

        updateStatus();
    }

    private void updateStatus() {
        if (cryptoBackupInteractor == null) {
            view.setStatusToError();
            return;
        }

        new AsyncTask<Void,Void,BackupStatus>() {
            @Override
            protected void onPreExecute() {
                view.setStatusToOverview();
            }

            @Override
            protected BackupStatus doInBackground(Void... voids) {
                return cryptoBackupInteractor.checkForBackup();
            }

            @Override
            protected void onPostExecute(BackupStatus status) {
                updateBackupStatus(status);
            }
        }.execute();
    }

    private void updateBackupStatus(BackupStatus status) {
        currentBackupStatus = status;
        if (view == null) {
            return;
        }

        switch (currentBackupStatus.statusType) {
            case UNSUPPORTED:
                view.setStatusToError();
                break;
            case ERROR:
                view.setStatusToError();
                break;
        }
    }

    public void onDestroy() {
        view = null;
    }

    public void onClickTransferReceive() {
        view.setStatusToPending();
    }

    public void onClickTransferSend() {
        if (currentBackupStatus.statusType == BackupStatusType.UNSUPPORTED) {
            throw new IllegalStateException("Click on backup now shouldn't happen if status is unsupported!");
        }
        startOrContinueBackup(null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_BACKUP) {
            throw new IllegalArgumentException("Unhandled state!");
        }

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        startOrContinueBackup(data);
    }

    private void startOrContinueBackup(Intent intent) {
        InteractionRequestOrResult<BackupDataResult> result;
        if (intent == null) {
            result = openPgpSimpleApi.actionBackup(true, account.getCryptoKey());
        } else {
            result = openPgpSimpleApi.actionBackupContinue(intent);
        }

        if (result.isPendingUserInteraction()) {
            view.launchUserInteractionPendingIntent(result.getUserInteractionIntent(), REQUEST_CODE_BACKUP);
            return;
        }

        byte[] backupData = result.getResult().getBytes();
        createBackupAndUpdateDisplay(backupData);
    }

    private void createBackupAndUpdateDisplay(final byte[] backupData) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected void onPreExecute() {
                view.setStatusToOverview();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                cryptoBackupInteractor.createBackupFile(backupData);
                return null;
            }

            @Override
            protected void onPostExecute(Void status) {
                updateStatus();
            }
        }.execute();
    }
}
