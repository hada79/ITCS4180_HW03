package com.jasonhada.homework03;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    SeekBar passwordCount_sb, passwordLength_sb;
    TextView passwordCount_tv, passwordLength_tv, password_tv;

    ExecutorService threadPool;
    Handler handler;
    ProgressDialog progressDialog;

    CharSequence[] passwords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threadPool = Executors.newFixedThreadPool(2);

        passwordCount_tv = (TextView) findViewById(R.id.passwordCount_tv);
        passwordCount_sb = (SeekBar) findViewById(R.id.passwordCount_sb);
        passwordCount_tv.setText(String.valueOf(passwordCount_sb.getProgress() + 1));
        passwordCount_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordCount_tv.setText(String.valueOf(passwordCount_sb.getProgress() + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        passwordLength_tv = (TextView) findViewById(R.id.passwordLength_tv);
        passwordLength_sb = (SeekBar) findViewById(R.id.passwordLength_sb);
        passwordLength_tv.setText(String.valueOf(passwordLength_sb.getProgress() + 8));
        passwordLength_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordLength_tv.setText(String.valueOf(passwordLength_sb.getProgress() + 8));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // THREAD POOL
        findViewById(R.id.thread_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(passwordCount_tv.getText().toString());
                int length = Integer.parseInt(passwordLength_tv.getText().toString());
                threadPool.execute(new GeneratePasswords(count, length));
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Progress");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        final TextView password = findViewById(R.id.password_tv);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Passwords").setItems(passwords, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                password.setText(passwords[which]);
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch(msg.what) {
                    case GeneratePasswords.STATUS_START:
                        progressDialog.setProgress(0);
                        progressDialog.show();
                        Log.d("demo", "Message starting ... ");
                        break;
                    case GeneratePasswords.STATUS_STOP:
                        progressDialog.dismiss();
                        Log.d("demo", "Message stopping ... ");
                        alert.show();
                        break;
                    case GeneratePasswords.STATUS_PROGRESS:
                        progressDialog.setProgress(msg.getData().getInt(GeneratePasswords.PROGRESS_KEY));
                        Log.d("demo", "Message progress ... " + msg.getData().getInt(GeneratePasswords.PROGRESS_KEY));
                        break;
                }
                return false;
            }

        });

        // ASYNC TASK
        Button async_btn = (Button) findViewById(R.id.async_btn);

        async_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GeneratePasswordsAsync().execute();
            }
        });
    }


    class GeneratePasswords implements  Runnable {

        static final int STATUS_START = 0x00;
        static final int STATUS_PROGRESS = 0x01;
        static final int STATUS_STOP = 0x02;
        static final String PROGRESS_KEY = "PROGRESS";

        int count, length;
        GeneratePasswords(int count, int length) {
            this.count = count;
            this.length = length;
        }

        @Override
        public void run() {

            Message startMessage = new Message();
            startMessage.what = STATUS_START;
            handler.sendMessage(startMessage);

            for (int i=0; i <= 100; i++) {
                passwords = new CharSequence[count];
                String password = Util.getPassword(length);
                for (int j = 0; j < count; j++) {
                    passwords[j] = password;
                }

                Util.getPassword(length);

                Message message = new Message();
                message.what = STATUS_PROGRESS;
                message.obj = i;
                Bundle bundle = new Bundle();
                bundle.putInt(PROGRESS_KEY, i);
                message.setData(bundle);
                handler.sendMessage(message);
            }

            Message stopMessage = new Message();
            stopMessage.what = STATUS_STOP;
            handler.sendMessage(stopMessage);
        }
    }

    class GeneratePasswordsAsync extends AsyncTask<CharSequence[], Integer, Integer> {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        CharSequence[] passwords;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Updating Progress");
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progressDialog.dismiss();

            final TextView password_set = findViewById(R.id.password_tv);

            alert.setTitle("Passwords").setItems(passwords, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    password_set.setText(passwords[which]);
                }
            });
            alert.create();
            alert.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected Integer doInBackground(CharSequence[]... strings) {
            SeekBar count_sb = findViewById(R.id.passwordCount_sb);
            SeekBar length_sb = findViewById(R.id.passwordLength_sb);

            int count = count_sb.getProgress();
            int length = length_sb.getProgress();

            for (int i = 0; i <= 100; i ++){
                for (int j = 0; j < count; j++){
                    passwords = new CharSequence[count];
                    String password = Util.getPassword(length);
                    passwords[j] = password;
                }
                publishProgress(i);
            }

            return 0;
        }
    }
}
