package com.fsck.k9.mailstore.interactor;


import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.K9;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Body;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Folder.FolderType;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.MimeHeader;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.internet.MimeMessageHelper;
import com.fsck.k9.mail.store.imap.ImapFolder;
import com.fsck.k9.mail.store.imap.ImapStore;
import com.fsck.k9.mailstore.BinaryMemoryBody;
import com.fsck.k9.mailstore.LocalMessage;
import com.fsck.k9.mailstore.LocalStore;
import com.fsck.k9.search.LocalSearch;
import com.fsck.k9.search.SearchSpecification.Attribute;
import com.fsck.k9.search.SearchSpecification.SearchField;
import okio.ByteString;


class ImapCryptoBackupInteractor extends CryptoBackupInteractor {
    private final Context context;
    private Account account;
    private final LocalStore localStore;
    private final ImapStore remoteStore;


    ImapCryptoBackupInteractor(Context context, Account account, LocalStore localStore, ImapStore remoteStore) {
        super();
        this.context = context;
        this.account = account;
        this.localStore = localStore;
        this.remoteStore = remoteStore;
    }

    @Override
    @NonNull
    @WorkerThread
    public BackupStatus checkForBackup() {
        try {
            ImapFolder folder = remoteStore.getFolder(BACKUP_IMAP_FOLDER_NAME);
            if (!folder.exists()) {
                return new BackupStatus(BackupStatusType.EMPTY, null);
            }

            MessagingController.getInstance(context).synchronizeMailboxSynchronous(
                    account, BACKUP_IMAP_FOLDER_NAME, null, null);

            LocalSearch localSearch = new LocalSearch();
            localSearch.and(SearchField.FOLDER, BACKUP_IMAP_FOLDER_NAME, Attribute.EQUALS);
            localSearch.and(SearchField.MIME_TYPE, "application/pgp-encrypted-keys", Attribute.EQUALS);

            List<LocalMessage> localMessages = localStore.searchForMessages(null, localSearch);

            if (localMessages.isEmpty()) {
                return new BackupStatus(BackupStatusType.EMPTY, null);
            }

            return new BackupStatus(BackupStatusType.OK, localMessages);
        } catch (MessagingException e) {
            Log.e(K9.LOG_TAG, "Erro searching for crypto message");
            return new BackupStatus(BackupStatusType.ERROR, null);
        }
    }

    @Override
    @WorkerThread
    public void createBackupFile(byte[] data) {
        MimeMessage message = createBackupMessage(data);

        try {
            saveMessageToBackupFolder(message);
        } catch (MessagingException e) {
            Log.e(K9.LOG_TAG, "error writing backup!", e);
        }
    }

    private void saveMessageToBackupFolder(MimeMessage message) throws MessagingException {
        ImapFolder folder = remoteStore.getFolder(BACKUP_IMAP_FOLDER_NAME);

        folder.open(Folder.OPEN_MODE_RW);
        createFolderIfNotExists(folder);

        folder.appendMessages(Collections.singletonList(message));
        MessagingController.getInstance(context).synchronizeMailboxSynchronous(
                account, BACKUP_IMAP_FOLDER_NAME, null, folder);
    }

    private MimeMessage createBackupMessage(byte[] data) {
        try {
            MimeMessage message = new MimeMessage();

            Body body = new BinaryMemoryBody(ByteString.of(data).base64().getBytes(), "base64");
            MimeMessageHelper.setBody(message, body);
            message.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "application/pgp-encrypted-keys");

            Date nowDate = new Date();

            message.setFlag(Flag.X_DOWNLOADED_FULL, true);
            message.setSubject("OpenPGP Keys Backup from " + nowDate.getTime());
            message.setInternalDate(nowDate);
            message.addSentDate(nowDate, K9.hideTimeZone());
            message.setFrom(new Address(account.getEmail()));

            return message;
        } catch (MessagingException e) {
            throw new AssertionError(e);
        }
    }

    private void createFolderIfNotExists(ImapFolder folder) throws MessagingException {
        folder.create(FolderType.HOLDS_MESSAGES);
        try {
            boolean aclSetOk = folder.setAclPermission("-l");
            if (!aclSetOk) {
                Log.e(K9.LOG_TAG, "Could not set ACL for backup folder (ACL not supported?)");
            }
        } catch (MessagingException e) {
            Log.e(K9.LOG_TAG, "Error setting ACL for backup folder", e);
        }
    }

}
