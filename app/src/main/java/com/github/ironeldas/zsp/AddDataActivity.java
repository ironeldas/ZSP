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
                        new DownloadValueTask("strom").execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (NullPointerException npe) {
            Toast.makeText(getApplicationContext(), "Couldn't set Listener.", Toast.LENGTH_SHORT).show();
        }

        Button loadWater = (Button) findViewById(R.id.load_water);
        try {
            loadWater.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new DownloadValueTask("wasser").execute();
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
                        new UploadValueTask("strom").execute(new Double(savePower.getText().toString()));
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (NullPointerException npe) {
            Toast.makeText(getApplicationContext(), "Couldn't set Listener.", Toast.LENGTH_SHORT).show();
        }

        final Button saveWater = (Button) findViewById(R.id.save_water);
        try {
            saveWater.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        new UploadValueTask("wasser").execute(new Double(saveWater.getText().toString()));
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (NullPointerException npe) {
            Toast.makeText(getApplicationContext(), "Couldn't set Listener.", Toast.LENGTH_SHORT).show();
        }
    }

    private class UploadValueTask extends AsyncTask<Double, Void, String> {
        private String url;
        private String type;

        public UploadValueTask(String type) {
            super();
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            String ip = "192.168.43.111";
            String port = "821";

            Calendar cal = Calendar.getInstance();
            String assembleddate = String.valueOf(cal.get(Calendar.YEAR)) + String.valueOf(cal.get(Calendar.MONTH)) + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

            this.url = "http://" + ip + ":" + port + "/consumption?typ=" + this.type + "&date=" + assembleddate + "&value=";
        }

        @Override
        protected String doInBackground(Double... d) {

            d[0] = (double) Math.round(d[0] * 100d) / 100d;
            this.url = this.url + d[0];

            try {
                HttpURLConnection conn = (HttpURLConnection) (new URL(this.url)).openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();

                Reader reader;
                reader = new InputStreamReader(is, "UTF-8");
                char[] buffer = new char[50];
                reader.read(buffer);

                is.close();
                reader.close();
                return new String(buffer);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String answer) {
            if (answer.equals("OK"))
                Toast.makeText(getApplicationContext(), "Upload successful.", Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadValueTask extends AsyncTask<Void, Void, String> {
        private String url;
        private String type;
        /**
         * Wenn value den Wert -1 hat, bedeutet das, dass es sich um einen Download von Werten handelt.
         */
        private int value;

        public DownloadValueTask(String type, int value) {
            super();
            this.type = type;
            this.value = value;
        }

        @Override
        protected void onPreExecute() {
            if (value == -1) {
                this.url = "http://" + ip + ":" + port + "/getconsumption?typ=" + this.type;
            } else {
                Calendar cal = Calendar.getInstance();
                String assembleddate = String.valueOf(cal.get(Calendar.YEAR)) + String.valueOf(cal.get(Calendar.MONTH)) + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

                this.url = "http://" + ip + ":" + port + "/consumption?typ=" + this.type + "&date=" + assembleddate + "&value=" + value;
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
            if (value == -1) {
                switch (this.type) {
                    case "strom":
                        EditText etStrom = (EditText) findViewById(R.id.et_power);
                        etStrom.setText(answer);
                        break;
                    case "wasser":
                        EditText etWasser = (EditText) findViewById(R.id.et_water);
                        etWasser.setText(answer);
                        break;
                }
            } else {
                if (answer != "OK") Toast.makeText(getApplicationContext(), "Couldn't upload values for " + type, Toast.LENGTH_SHORT);
            }
        }
    }
}
