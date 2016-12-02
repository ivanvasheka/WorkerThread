package com.ivanvasheka.workerthreadsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivanvasheka.workerthread.Event;
import com.ivanvasheka.workerthread.EventListener;
import com.ivanvasheka.workerthread.WorkerThread;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EventListener {

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

        if (!WorkerThread.get().isRunning(Task2.TAG)) {
            WorkerThread.get().execute(new Task2(), Task2.TAG);
        } else {
            Log.d(Task2.TAG, "Task2 is still running no need to start new one.");
        }

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

    @Override
    public void onEvent(Event event) {
        Log.e(TAG, "onEvent " + event.toString() + " in " + MainActivity.this);

        if (event instanceof CustomEvent) {
            Log.e(TAG, "Custom event with T = " + ((CustomEvent) event).T);
        }

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
            WorkerThread.get().invalidate(MainActivity.class);

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

            WorkerThread.get().post(new CustomEvent());
        }
    }

    private static class Task2 implements Runnable {

        public static String TAG = "TASK-2";

        @Override
        public void run() {
            Log.d(Task2.TAG, "Task2: starting...");
            Random random = new Random();
            for (int i = 0; i < 500; i++) {
                int randomInt = random.nextInt(50);

                try {
                    Thread.sleep(randomInt);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(Task2.TAG, "Task2: finishing...");

            Event.to(MainActivity.class)
                    .withNumber(2)
                    .post();
        }
    }
}
