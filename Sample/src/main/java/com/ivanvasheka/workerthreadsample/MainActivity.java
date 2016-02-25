package com.ivanvasheka.workerthreadsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivanvasheka.workerthread.Event;
import com.ivanvasheka.workerthread.WorkerThread;
import com.ivanvasheka.workerthread.annotation.Subscribe;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "WT-SAMPLE";

    TextView textView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        findViewById(R.id.execute_simple).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WorkerThread.get().subscribe(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        WorkerThread.get().unsubscribe(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.execute_simple:
                progressBar.setIndeterminate(true);
                Log.e(TAG, "Start in " + MainActivity.this);
                WorkerThread.get().execute(new Task());
                break;
        }
    }

    @Subscribe
    public void onEvent(Event event) {
        Log.e(TAG, "onEvent " + event.toString() + " in " + MainActivity.this);

        if (progressBar.isIndeterminate()) {
            progressBar.setIndeterminate(false);
        }
        if (event.getNumber() != null) {
            textView.setText(String.valueOf(event.getNumber()));
            progressBar.setProgress(event.getNumber().intValue());
        } else {
            Log.e(TAG, "Done in " + MainActivity.this);

            textView.setText("Done!");
            progressBar.setProgress(0);
        }
    }

    private static class Task implements Runnable {
        @Override
        public void run() {
            Random random = new Random();
            for (int i = 0; i < 1000; i++) {
                try {
                    Thread.sleep(random.nextInt(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Event.to(MainActivity.class)
                        .withType(Event.TYPE_LATEST_ONLY)
                        .withNumber(i / 10)
                        .post();
            }

            Event.toEveryone().post();
        }
    }
}
