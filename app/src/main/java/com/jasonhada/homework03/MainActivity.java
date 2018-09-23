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

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    SeekBar passwordCount_sb, passwordLength_sb;
    TextView passwordCount_tv, passwordLength_tv, password_tv;

    ExecutorService threadPool;
    Handler handler;
    ProgressDialog progressDialog;

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

        final TextView password = findViewById(R.id.password_tv);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch(msg.what) {

                    case GeneratePasswords.STATUS_START:
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Updating Progress");
                        progressDialog.setMax(100);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgress(0);
                        progressDialog.show();
                        break;

                    case GeneratePasswords.STATUS_PROGRESS:

                        int progress = msg.getData().getInt(GeneratePasswords.PROGRESS_KEY);
                        progressDialog.setProgress(progress);
                        break;

                    case GeneratePasswords.STATUS_STOP:

                        progressDialog.dismiss();
                        final String[] passwords = msg.getData().getStringArray("passwords");

                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Select a password")
                            .setItems(passwords, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    password.setText(passwords[which]);
                            }
                        });
                        alert.show();
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

            String[] passwords = new String[count];
            Message startMessage = new Message();
            startMessage.what = STATUS_START;
            handler.sendMessage(startMessage);

            // this loops for the number of passwords that need to be created.
            for (int i=0; i<count; i++) {
                String password = Util.getPassword(length);

                passwords[i] = password;

                // message to handler to notify progress
                Message message = new Message();
                message.what = STATUS_PROGRESS;
                Bundle bundle = new Bundle();
                int progress = (int) ((i+1) * 100.0f / count);
                bundle.putInt(PROGRESS_KEY, progress);
                message.setData(bundle);
                handler.sendMessage(message);
            }

            // message to handler when complete with passwords
            Message stopMessage = new Message();
            stopMessage.what = STATUS_STOP;
            Bundle bundle = new Bundle();
            bundle.putStringArray("passwords", passwords);
            stopMessage.setData(bundle);
            handler.sendMessage(stopMessage);
        }
    }

    class GeneratePasswordsAsync extends AsyncTask<CharSequence[], Integer, String[]> {

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
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected String[] doInBackground(CharSequence[]... strings) {
            SeekBar count_sb = findViewById(R.id.passwordCount_sb);
            SeekBar length_sb = findViewById(R.id.passwordLength_sb);

            int count = count_sb.getProgress() + 1 ;
            int length = length_sb.getProgress() + 8;

            String [] passwords = new String[count];

            for (int i = 0; i < count; i ++){

                String password = Util.getPassword(length);
                passwords[i] = password;

                int progress = (int) ((i+1) * 100.0f / count);
                publishProgress(progress);
            }

            return passwords;
        }

        @Override
        protected void onPostExecute(final String[] passwords) {
            progressDialog.dismiss();

            final TextView password_set = findViewById(R.id.password_tv);

            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Select a password")
                    .setItems(passwords, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            password_set.setText(passwords[which]);
                        }
                    });
            alert.show();
        }
    }
}
