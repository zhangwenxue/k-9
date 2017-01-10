package com.fsck.k9.ui.crypto.backup;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fsck.k9.R;
import com.fsck.k9.mailstore.LocalMessage;


public class CryptoBackupMessageAdapter extends ArrayAdapter<LocalMessage> {

    private final LayoutInflater inflater;
    private final OnClickRestoreBackupListener onClickRestoreBackupListener;

    public CryptoBackupMessageAdapter(Context context, OnClickRestoreBackupListener onClickRestoreBackupListener) {
        super(context, R.layout.crypto_backup_item);

        inflater = LayoutInflater.from(context);
        this.onClickRestoreBackupListener = onClickRestoreBackupListener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.crypto_backup_item, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        LocalMessage message = getItem(position);

        String formattedDate = DateFormat.getDateFormat(getContext()).format(message.getSentDate());
        vh.title.setText(formattedDate);

        final String messageUid = message.getUid();
        vh.restoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRestoreBackupListener.onClickRestore(messageUid);
            }
        });

        return convertView;
    }

    public interface OnClickRestoreBackupListener {
        void onClickRestore(String messageUid);
    }

    private static class ViewHolder {
        private final View restoreButton;
        private final TextView title;

        ViewHolder(View v) {
            title = ((TextView) v.findViewById(R.id.backup_title));
            restoreButton = v.findViewById(R.id.backup_restore_button);
        }
    }
}
