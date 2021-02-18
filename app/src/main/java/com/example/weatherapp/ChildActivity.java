package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static com.example.bottomnavigationtest.NetwirkUtils.generateURL_OWM;
import static com.example.bottomnavigationtest.NetwirkUtils.generateURL_SINOPTIK;
import static com.example.bottomnavigationtest.NetwirkUtils.getResponseFromUrl;
import static com.example.bottomnavigationtest.NetwirkUtils.getResponseFromUrl_1;

public class ChildActivity extends AppCompatActivity {

    private EditText textEntry;
    private Button changeActivityButton;
    private TextView textView1;



    private EditText search_field;
    private Button search_button;
    private Button change_city_button;
    private Button backButton;
    private TextView search_results_sin;
    private TextView search_results_owm;
    private TextView errorUnknownCityMessage;
    private TextView otherErrorMessage;
    private TextView current_city_field;
    private ProgressBar loading;
    private CheckBox mCheckBox1;

    String chosen_city;
    String temp_sin = null;
    String humidity_sin = null;
    String pressure_sin =null;




    private void showResultTextViev(){
        otherErrorMessage.setVisibility(View.INVISIBLE);
        errorUnknownCityMessage.setVisibility(View.INVISIBLE);
        search_results_sin.setVisibility(View.VISIBLE);
        search_results_owm.setVisibility(View.VISIBLE);
    }

    private void showOtherErrorTextViewSin(){
        search_results_sin.setText(getString(R.string.other_error_message));
        errorUnknownCityMessage.setVisibility(View.INVISIBLE);
    }

    private void showOtherErrorTextViewOwm(){
        search_results_owm.setText(getString(R.string.other_error_message));
        errorUnknownCityMessage.setVisibility(View.INVISIBLE);
        search_results_owm.setVisibility(View.VISIBLE);
    }

    private void showCityErrorTextViewSin(){
        search_results_sin.setText(getString(R.string.unknown_city_error_message));
        otherErrorMessage.setVisibility(View.INVISIBLE);


    }

    private void showCityErrorTextViewOwm(){
        search_results_owm.setText(getString(R.string.unknown_city_error_message));
        otherErrorMessage.setVisibility(View.INVISIBLE);

    }

    private void showCurCity(){
        current_city_field.setVisibility(View.VISIBLE);
        change_city_button.setVisibility(View.VISIBLE);
        search_field.setVisibility(View.INVISIBLE);
        search_button.setVisibility(View.INVISIBLE);
        mCheckBox1.setVisibility(View.INVISIBLE);
    }

    private void showInputCity(){
        current_city_field.setVisibility(View.INVISIBLE);
        change_city_button.setVisibility(View.INVISIBLE);
        search_field.setVisibility(View.VISIBLE);
        search_button.setVisibility(View.VISIBLE);
        mCheckBox1.setVisibility(View.VISIBLE);
    }

    class WQueryTask_SINOPTIK extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            String response = null;
            try {
                response = getResponseFromUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            String mainWeather = null;
           
            System.out.println("RESPONSE =="+response);
            if (response != null && response.equals("404")){
                showCityErrorTextViewSin();
            }else if (response != null && !response.equals("")) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    temp_sin = jsonResponse.getString("temperature");
                    humidity_sin = jsonResponse.getString("humidity");
                    pressure_sin = jsonResponse.getString("pressure");
                    String status = jsonResponse.getString("status");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String resultString = "Sinoptik"+"\n"+"Temperature: " + temp_sin + "\n" + "Humidity: " + humidity_sin+"%"+ "\n" + "Pressure: " + pressure_sin + "mm Hg" + "\n";
                search_results_sin.setText(resultString);

                showResultTextViev();
            } else {
                showOtherErrorTextViewSin();
            }
            loading.setVisibility(View.INVISIBLE);

        }
    }

    class WQueryTask_OWM extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            String response = null;
            try {
                response = getResponseFromUrl_1(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(String response) {
            String temp = null;
            String humidity = null;
            String pressure =null;
            String mainWeather = null;
           
            System.out.println("RESPONSE =="+response);
            if (response != null && response.equals("404")){
                showCityErrorTextViewOwm();
            }else if (response != null && !response.equals("")) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    mainWeather = jsonResponse.getString("main");
                    JSONObject weatherInfo = new JSONObject(mainWeather);
                    temp = weatherInfo.getString("temp");
                    humidity = weatherInfo.getString("humidity");
                    pressure = weatherInfo.getString("pressure");
                    double pressure_double = Double.parseDouble(pressure);
                    pressure_double= pressure_double * 0.75006;
                    pressure = String.format("%.2f", pressure_double);
                    String sign = temp.substring(0, 1);
                    String sign_minus="-";
                    if (!sign.equals(sign_minus) ){
                        temp  = "+" + temp;

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String resultString = "Open weather"+"\n"+"Temperature: " + temp +"\u00b0"+ "\n" + "Humidity: " + humidity +"%"+ "\n" + "Pressure: "+ pressure +"mm Hg";
                search_results_owm.setText(resultString);

                showResultTextViev();
            } else {
                showOtherErrorTextViewOwm();
            }
            loading.setVisibility(View.INVISIBLE);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.child_title);
        setContentView(R.layout.activity_child);

        backButton = findViewById(R.id.bt_back_to_main);

        search_field=findViewById(R.id.et_input_field);
        search_button=findViewById(R.id.b_search);
        search_results_sin = findViewById(R.id.tv_search_results_sin);
        search_results_owm = findViewById(R.id.tv_search_results_owm);
        otherErrorMessage = findViewById(R.id.tv_other_error_message);
        errorUnknownCityMessage = findViewById(R.id.tv_unknown_city_error_message);
        loading = findViewById(R.id.pb_loading);
        current_city_field = findViewById(R.id.tv_current_city);
        change_city_button = findViewById(R.id.b_change_city);
        mCheckBox1 = findViewById(R.id.cb_remember_city_checkbox);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                URL generatedUrl_SINOTIK = generateURL_SINOPTIK(search_field.getText().toString());
                URL generatedUrl_OWM = generateURL_OWM(search_field.getText().toString());
                new WQueryTask_SINOPTIK().execute(generatedUrl_SINOTIK);
                new WQueryTask_OWM().execute(generatedUrl_OWM);
                chosen_city = search_field.getText().toString();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if(mCheckBox1.isChecked() & !chosen_city.equals("")){
                    SharedPreferences sPref_city = getSharedPreferences("pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref_city.edit();
                    ed.putString("city", chosen_city);
                    ed.commit();
                    String city = chosen_city.substring(0, 1).toUpperCase() + chosen_city.substring(1);
                    current_city_field.setText("Your city: " + city);

                } else if (chosen_city.equals("")){

                    Toast.makeText(ChildActivity.this, "Input field is empty", Toast.LENGTH_LONG).show();
                }
                showCurCity();



            }
        };
        View.OnClickListener onClickListener_change = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputCity();
                search_field.requestFocus();
                search_field.setFocusableInTouchMode(true);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(search_field, InputMethodManager.SHOW_IMPLICIT);

            }
        };
        search_button.setOnClickListener(onClickListener);
        change_city_button.setOnClickListener(onClickListener_change);

        SharedPreferences sPref_city = getSharedPreferences("pref", Context.MODE_PRIVATE);
        String savedText = sPref_city.getString("city",null);
        if (savedText != null) {
            String city = savedText.substring(0, 1).toUpperCase() + savedText.substring(1);
            current_city_field.setText("Your city: " + city);
            showCurCity();
            URL generatedUrl_SINOTIK = generateURL_SINOPTIK(savedText);
            URL generatedUrl_OWM = generateURL_OWM(savedText);
            new WQueryTask_SINOPTIK().execute(generatedUrl_SINOTIK);
            new WQueryTask_OWM().execute(generatedUrl_OWM);
        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = ChildActivity.this;

                Class destinationActivity = com.example.weatherapp.MainActivity.class;

                Intent mainActivityIntent = new Intent(context, destinationActivity);


                finish();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        saveCity();

    }

    public void saveCity (){
        mCheckBox1 = findViewById(R.id.cb_remember_city_checkbox);
        search_field = findViewById(R.id.et_input_field);
        chosen_city = search_field.getText().toString();
        if(mCheckBox1.isChecked() & !chosen_city.equals("")){
            SharedPreferences sPref_city = getSharedPreferences("pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref_city.edit();
            ed.putString("city", chosen_city);
            ed.commit();

        }
    }
}