package com.fsck.k9.ui.crypto.backup;


import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fsck.k9.R;
import com.fsck.k9.mailstore.LocalMessage;


public class CryptoBackupMessageAdapter extends ArrayAdapter<LocalMessage> {

    private final LayoutInflater inflater;

    public CryptoBackupMessageAdapter(Context context) {
        super(context, R.layout.crypto_backup_item);

        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.crypto_backup_item, parent, false);
        }

        LocalMessage message = getItem(position);
        ((TextView) convertView.findViewById(R.id.backup_title)).setText(DateFormat.getDateFormat(getContext()).format(message.getSentDate()));

        return convertView;
    }
}
