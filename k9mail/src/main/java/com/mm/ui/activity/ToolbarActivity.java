package com.mm.ui.activity;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fsck.k9.R;

import butterknife.BindView;

public abstract class ToolbarActivity extends BaseActivity {
    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
    }
}
