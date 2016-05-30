package com.github.ironeldas.zsp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class AddDataActivity extends AppCompatActivity {

    private String ip = "192.168.43.111";
    private String port = "821";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        Button loadPower = (Button) findViewById(R.id.load_power);
        try {
            loadPower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new InterfereWithValueserverTask("strom", true).execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (NullPointerException npe) {
            Toast.makeText(getApplicationContext(), "Couldn't set Listener.", Toast.LENGTH_SHORT).show();
        }

        final Button savePower = (Button) findViewById(R.id.save_power);
        try {
            savePower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new InterfereWithValueserverTask("strom", false).execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (NullPointerException npe) {
            Toast.makeText(getApplicationContext(), "Couldn't set Listener.", Toast.LENGTH_SHORT).show();
        }
    }

    private class InterfereWithValueserverTask extends AsyncTask<Void, Void, String> {
        private String url;
        private String type;
        private boolean down;
        private double value;

        public InterfereWithValueserverTask(String type, boolean down) {
            super();
            this.type = type;
            this.down = down;
        }

        @Override
        protected void onPreExecute() {
            getValueFromView();

            if (down) {
                this.url = "http://" + ip + ":" + port + "/getconsumption?typ=" + this.type;
            } else {
                Calendar cal = Calendar.getInstance();
                String assembleddate = String.valueOf(cal.get(Calendar.YEAR)) + String.valueOf(cal.get(Calendar.MONTH)) + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

                this.url = "http://" + ip + ":" + port + "/consumption?typ=" + this.type + "&date=" + assembleddate + "&value=" + value;
            }
        }

        private void getValueFromView() {
            if (!down) {
                switch (this.type) {
                    case "strom":
                        EditText etStrom = (EditText) findViewById(R.id.et_power);
                        value = new Double(etStrom.getText().toString());
                        break;
                }
            }
        }

        @Override
        protected String doInBackground(Void... arg) {
            try {
                return openUrl();
            } catch (IOException e) {
                return "Unable to retrieve web page.";
            }
        }

        private String openUrl() throws IOException {
            InputStream inputStream = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) (new URL(this.url)).openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                inputStream = conn.getInputStream();

                return readStream(inputStream);
            } finally {
                if (inputStream != null)
                    inputStream.close();
            }
        }

        private String readStream(InputStream inputStream) throws IOException {

            Reader reader;
            reader = new InputStreamReader(inputStream, "UTF-8");
            char[] buffer = new char[50];
            reader.read(buffer);
            reader.close();
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String answer) {
            if (down) {
                switch (this.type) {
                    case "strom":
                        EditText etStrom = (EditText) findViewById(R.id.et_power);
                        etStrom.setText(answer);
                        break;
                }
            } else {
                if (answer != "OK")
                    Toast.makeText(getApplicationContext(), "Couldn't upload values for " + type, Toast.LENGTH_SHORT);
            }
        }
    }
}
