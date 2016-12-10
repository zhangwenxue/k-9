package com.fsck.k9.helper;


import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;


public abstract class AsyncTaskDelayed<Params,Progress,Result> extends AsyncTask<Params,Progress,Result> {
    final private long waitTimeMillis;

    public AsyncTaskDelayed(int waitTime, TimeUnit unit) {
        super();

        this.waitTimeMillis = unit.toMillis(waitTime);
    }

    @Override
    protected final Result doInBackground(Params[] params) {
        long startTime = System.currentTimeMillis();
        Result result = doInBackgroundWithDelay(params);
        long executionTime = System.currentTimeMillis() - startTime;

        if (executionTime < waitTimeMillis) {
            try {
                TimeUnit.MILLISECONDS.sleep(waitTimeMillis - executionTime);
            } catch (InterruptedException e) {
                // just continue
            }
        }

        return result;
    }

    protected abstract Result doInBackgroundWithDelay(Params... params);

}
