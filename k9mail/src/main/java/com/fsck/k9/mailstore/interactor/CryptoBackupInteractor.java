package com.fsck.k9.mailstore.interactor;


import java.util.Date;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.fsck.k9.Account;
import com.fsck.k9.Globals;
import com.fsck.k9.K9;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message.RecipientType;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.MimeBodyPart;
import com.fsck.k9.mail.internet.MimeHeader;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.internet.MimeMessageHelper;
import com.fsck.k9.mail.internet.MimeMultipart;
import com.fsck.k9.mail.internet.TextBody;
import com.fsck.k9.mailstore.BinaryMemoryBody;
import com.fsck.k9.mailstore.LocalMessage;
import com.fsck.k9.mailstore.LocalStore;
import com.fsck.k9.search.LocalSearch;
import com.fsck.k9.search.SearchSpecification.Attribute;
import com.fsck.k9.search.SearchSpecification.SearchField;


public class CryptoBackupInteractor {
    public static final String BACKUP_IMAP_FOLDER_NAME = "_well_known/_openpgp_backup";
    private final Context context;
    private final Account account;


    public static CryptoBackupInteractor getInstance(Account account) {
        Context context = Globals.getContext();
        return new CryptoBackupInteractor(context, account);
    }

    private CryptoBackupInteractor(Context context, Account account) {
        this.context = context;
        this.account = account;

    }

    @NonNull
    @WorkerThread
    public BackupStatus checkForBackup() {
        try {
            LocalStore localStore = account.getLocalStore();

            LocalSearch localSearch = new LocalSearch("Swag");
            localSearch.and(SearchField.SENDER, account.getEmail(), Attribute.EQUALS);
            localSearch.and(SearchField.TO, account.getEmail(), Attribute.EQUALS);
            localSearch.and(SearchField.SUBJECT, "Autocrypt", Attribute.CONTAINS);
            List<LocalMessage> localMessages = localStore.searchForMessages(null, localSearch);

            if (localMessages.isEmpty()) {
                return new BackupStatus(BackupStatusType.EMPTY, null);
            }

            return new BackupStatus(BackupStatusType.OK, localMessages);
        } catch (MessagingException e) {
            return new BackupStatus(BackupStatusType.ERROR, null);
        }
    }

    @WorkerThread
    public void createBackupFile(byte[] data) {
        MimeMessage message = createBackupMessage(data);

        MessagingController.getInstance(context).sendMessage(account, message, null);
    }

    private MimeMessage createBackupMessage(byte[] data) {
        try {
            MimeBodyPart textBodyPart = new MimeBodyPart(new TextBody("This message is sent by Autocrypt! Woo~"));
            MimeBodyPart dataBodyPart = new MimeBodyPart(new BinaryMemoryBody(data, "7bit"));
            dataBodyPart.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "application/pgp-encrypted-keys");

            MimeMultipart messageBody = MimeMultipart.newInstance();
            messageBody.addBodyPart(textBodyPart);
            messageBody.addBodyPart(dataBodyPart);

            MimeMessage message = new MimeMessage();
            MimeMessageHelper.setBody(message, messageBody);

            Date nowDate = new Date();

            message.setFlag(Flag.X_DOWNLOADED_FULL, true);
            message.setFlag(Flag.X_DELETE_AFTER_DELIVERY, true);
            message.setSubject("Autocrypt Setup Message");
            message.setInternalDate(nowDate);
            message.addSentDate(nowDate, K9.hideTimeZone());
            message.setFrom(new Address(account.getEmail()));
            message.setRecipients(RecipientType.TO, Address.parse(account.getEmail()));

            return message;
        } catch (MessagingException e) {
            throw new AssertionError(e);
        }
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
