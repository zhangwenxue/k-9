package com.fsck.k9.mailstore.interactor;


import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.fsck.k9.Account;
import com.fsck.k9.Globals;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Store;
import com.fsck.k9.mail.store.imap.ImapStore;
import com.fsck.k9.mailstore.LocalMessage;
import com.fsck.k9.mailstore.LocalStore;


public class CryptoBackupInteractor {
    public static final String BACKUP_IMAP_FOLDER_NAME = "_well_known/_openpgp_backup";


    public static CryptoBackupInteractor getInstance(Account account) {
        Context context = Globals.getContext();

        Store localStore, remoteStore;
        try {
            localStore = account.getLocalStore();
            remoteStore = account.getRemoteStore();
        } catch (MessagingException e) {
            e.printStackTrace();
            return null;
        }

        if (remoteStore instanceof ImapStore) {
            return new ImapCryptoBackupInteractor(context, account, (LocalStore) localStore, (ImapStore) remoteStore);
        }

        return new CryptoBackupInteractor();
    }

    CryptoBackupInteractor() { }

    @NonNull
    @WorkerThread
    public BackupStatus checkForBackup() {
        return new BackupStatus(BackupStatusType.UNSUPPORTED, null);
    }

    @WorkerThread
    public void createBackupFile(byte[] data) {
        throw new UnsupportedOperationException();
    }

    public enum BackupStatusType {
        UNSUPPORTED, ERROR, EMPTY, OK;
    }

    public static class BackupStatus {
        public final BackupStatusType statusType;
        public final List<LocalMessage> backupDate;

        BackupStatus(BackupStatusType statusType, List<LocalMessage> messages) {
            this.statusType = statusType;
            this.backupDate = messages;
        }
    }
}
