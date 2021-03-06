package com.cm.primail;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.cm.primail.util.EmailAddressValidator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

public class AddAccountsActivity extends AppCompatActivity {

    @BindView(R.id.email)
    AutoCompleteTextView email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.email_input_layout)
    TextInputLayout emailInputLayout;
    @BindView(R.id.psw_input_layout)
    TextInputLayout pswInputLayout;
    private EmailAddressValidator mEmailValidator;
    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_accounts);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mEmailValidator = new EmailAddressValidator();
        mLoadingDialog = new ProgressDialog(this);
    }

    @OnTextChanged({R.id.email})
    void onEmailTextChanged(CharSequence s, int start, int before, int count) {
        isMailAddressValid();
    }

    @OnTextChanged({R.id.password})
    void onPswTextChanged(CharSequence s, int start, int before, int count) {
        isPswValid();
    }

    @OnFocusChange({R.id.email, R.id.password})
    void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.email:
                if (hasFocus) {
                    emailInputLayout.setErrorEnabled(false);
                } else {
                    isMailAddressValid();
                }
                break;
            case R.id.password:
                if (hasFocus) {
                    pswInputLayout.setErrorEnabled(false);
                } else {
                    isPswValid();
                }
                break;
            default:
        }
    }

    @OnEditorAction(R.id.password)
    boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
            attemptLogin();
            return true;
        }
        return false;
    }

    private boolean isMailAddressValid() {
        String emailAddress = email.getText().toString().trim();
        if (TextUtils.isEmpty(emailAddress)) {
            emailInputLayout.setError("邮箱不能为空");
            return false;
        }
        if (!mEmailValidator.isValidAddressOnly(emailAddress)) {
            emailInputLayout.setError("邮箱格式不正确");
            return false;
        }
        emailInputLayout.setErrorEnabled(false);
        return true;
    }

    private boolean isPswValid() {
        String psw = password.getText().toString().trim();
        if (TextUtils.isEmpty(psw)) {
            pswInputLayout.setError("邮箱密码不能为空");
            return false;
        }
        pswInputLayout.setErrorEnabled(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next_step:
                attemptLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void attemptLogin() {
        boolean isMailAddressValid = isMailAddressValid();
        boolean isPswValid = isPswValid();
        if (!isMailAddressValid || !isPswValid) {
            return;
        }
        mLoadingDialog.show();
        Log.i("", "");
    }
}
