package com.cm.primail.ui.dialog;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cm.primail.R;

public class LoadingDialogFragment extends DialogFragment {


    private String message;
    private boolean cancelable = true;
    private boolean cancelOnTouchOutside = false;

    public LoadingDialogFragment() {
    }

    public static LoadingDialogFragment newInstance(Object... args) {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        if (args != null && args.length > 0) {
            int length = args.length;
            Bundle bundle = new Bundle();
            String message = (String) args[0];
            bundle.putString("message", message);
            if (length > 1) {
                boolean cancelable = (boolean) args[1];
                bundle.putBoolean("cancelable", cancelable);
            }
            if (length > 2) {
                boolean cancelOnTouchOutside = (boolean) args[2];
                bundle.putBoolean("cancelOnTouchOutside", cancelOnTouchOutside);
            }
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            message = getArguments().getString("message", "");
            cancelable = getArguments().getBoolean("cancelable", true);
            cancelOnTouchOutside = getArguments().getBoolean("cancelOnTouchOutside", false);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.LoadingDialog);
        @SuppressLint("InflateParams")
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dlg_loading, null);
        dialog.setContentView(contentView);
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = 360;
        p.height = 180;
        dialog.getWindow().setAttributes(p);
        if (!TextUtils.isEmpty(message)) {
            TextView msgView = contentView.findViewById(R.id.message);
            msgView.setText(message);
        }
        return dialog;
    }
}
