package org.openintents.openpgp.util;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpServiceConnection.OnBound;


public class OpenPgpSimpleApi {
    private final Context context;
    private final String cryptoProvider;

    private volatile OpenPgpApi openPgpApi;

    public static OpenPgpSimpleApi getInstance(Context context, String cryptoProvider) {
        return new OpenPgpSimpleApi(context, cryptoProvider);
    }

    private OpenPgpSimpleApi(final Context context, String cryptoProvider) {
        this.context = context;
        this.cryptoProvider = cryptoProvider;

        connectToService();
    }

    private void connectToService() {
        final OpenPgpServiceConnection openPgpServiceConnection = new OpenPgpServiceConnection(context, cryptoProvider,
                new OnBound() {
                    @Override
                    public void onBound(IOpenPgpService2 service) {
                        openPgpApi = new OpenPgpApi(context, service);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(OpenPgpApi.TAG, "Couldn't connect to OpenPgpService", e);
                    }
                });
        openPgpServiceConnection.bindToService();
    }

    private void waitUntilConnected() {
        if (openPgpApi == null) {
            connectToService();
        }
    }

    public InteractionRequestOrResult<BackupDataResult> actionBackup(boolean backupSecret, long masterKeyId) {
        Intent intent = new Intent(OpenPgpApi.ACTION_BACKUP);
        intent.putExtra(OpenPgpApi.EXTRA_BACKUP_SECRET, backupSecret);
        intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
        intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, new long[] { masterKeyId });

        return actionBackupContinue(intent);
    }

    public InteractionRequestOrResult<BackupDataResult> actionBackupContinue(Intent intent) {
        waitUntilConnected();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Intent result = openPgpApi.executeApi(intent, (InputStream) null, byteArrayOutputStream);

        switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                return new InteractionRequestOrResult<>(result.<PendingIntent>getParcelableExtra(OpenPgpApi.RESULT_INTENT));

            case OpenPgpApi.RESULT_CODE_ERROR:
                return new InteractionRequestOrResult<>(result.<OpenPgpError>getParcelableExtra(OpenPgpApi.RESULT_ERROR));
        }

        return new InteractionRequestOrResult<>(new BackupDataResult(byteArrayOutputStream.toByteArray()));
    }

    public class InteractionRequestOrResult<T extends OpenPgpResult> {
        private final PendingIntent interactionRequest;
        private final T result;
        private final OpenPgpError openPgpError;

        InteractionRequestOrResult(PendingIntent interactionRequest) {
            this.interactionRequest = interactionRequest;
            this.result = null;
            this.openPgpError = null;
        }

        InteractionRequestOrResult(T result) {
            this.interactionRequest = null;
            this.result = result;
            this.openPgpError = null;
        }

        public InteractionRequestOrResult(OpenPgpError openPgpError) {
            this.interactionRequest = null;
            this.result = null;
            this.openPgpError = openPgpError;
        }

        public boolean isPendingUserInteraction() {
            return interactionRequest != null;
        }

        public PendingIntent getUserInteractionIntent() {
            return interactionRequest;
        }

        public T getResult() {
            return result;
        }
    }

    abstract class OpenPgpResult {

    }

    abstract class DataResult extends OpenPgpResult {
        final private byte[] resultBytes;

        DataResult(byte[] resultBytes) {
            this.resultBytes = resultBytes;
        }

        public byte[] getBytes() {
            return resultBytes;
        }
    }

    public class BackupDataResult extends DataResult {

        BackupDataResult(byte[] resultBytes) {
            super(resultBytes);
        }
    }

}
