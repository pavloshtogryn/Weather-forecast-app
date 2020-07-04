package com.example.weatherapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.bottomnavigationtest.NetwirkUtils.generateURL_SINOPTIK;
import static com.example.bottomnavigationtest.NetwirkUtils.getResponseFromUrl;



public class MainActivity extends AppCompatActivity {

    private TextView message_bar;
    private TextView pressure;
    private TextView humidity;
    private TextView forecast;
    private final static int REQUEST_ENABLE_BT = 1;
    Button btnPaired;
    Button btnDiscovery;
    Button btnCancelDiscovery;
    Button btnReconnect;
    //static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;
    private CheckBox mCheckBox;
    //Handler handler7 = new Handler();
    String dest_mac;
    boolean forecast_flag = false;
    //boolean async_task_flag = false;
    String global_press;
    String global_avg_press;

    SharedPreferences sPref1;
    final String SAVED_TEXT = "";

    Handler h;
    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private StringBuilder sb = new StringBuilder();


    private Button changeActivityButton;

    class getWindSINOPTIK extends AsyncTask<URL, Void, String> {
        //@Override
        //protected void onPreExecute() {
        //  loading.setVisibility(View.VISIBLE);
        //}

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
           /* search_results_sin.setVisibility(View.VISIBLE);
            search_results_sin.setText(response);
        }
    }*/
            System.out.println("RESPONSE =="+response);
            //Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
            if (response != null && response.equals("404")){
                forecast.setText(R.string.unknown_city_error_message);
                //forecast_flag = false;
                //showCityErrorTextViewSin();
            }else if (response != null && !response.equals("")) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String wind = jsonResponse.getString("wind");
                    make_forecast(wind);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //String resultString = "Sinoptik"+"\n"+"Temperature: " + temp_sin + "\n" + "Humidity: " + humidity_sin+"%"+ "\n" + "Pressure: " + pressure_sin + "mm Hg" + "\n";

                //forecast.setText(wind);
                //search_results_sin.setText(resultString);

                //showResultTextViev();
            } else {
                forecast.setText(R.string.other_error_message);
                //forecast_flag = false;
                //showOtherErrorTextViewSin();
            }
            //loading.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changeActivityButton =findViewById(R.id.bt_change_activity);

        changeActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = MainActivity.this;

                Class destinationActivity = com.example.weatherapp.ChildActivity.class;

                Intent childActivityIntent = new Intent(context, destinationActivity);

                startActivity(childActivityIntent);
            }
        });
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnPaired = findViewById(R.id.b_choose_bonded);
        btnDiscovery = findViewById(R.id.b_choose_discovery);
        btnCancelDiscovery = findViewById(R.id.b_cancel_discovery);
        btnReconnect = findViewById(R.id.b_reconnect);
        message_bar = findViewById(R.id.tv_message_bar);
        pressure = findViewById(R.id.tv_pressure);
        humidity = findViewById(R.id.tv_humidity);
        forecast = findViewById(R.id.tv_forecast);
        //message_bar.setVisibility(View.INVISIBLE);
        //String status;
        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

        if (bluetoothAdapter == null) {
            message_bar.setVisibility(View.VISIBLE);
        } else
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            //flag=true;
            message_bar.setText(R.string.bluetooth_enabled);
            message_bar.setVisibility(View.VISIBLE);

        }
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            //Toast.makeText(this, "Permission has already been granted", Toast.LENGTH_LONG).show();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
            LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            if(!gps_enabled && !network_enabled) {
                // notify user
                new AlertDialog.Builder(this)
                        .setMessage(R.string.gps_network_not_enabled)
                        .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                                .setNegativeButton(R.string.Cancel,null)
                                .show();
            }
            //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //startActivity(intent);
        }
        //String mydeviceaddress= bluetoothAdapter.getAddress();
        //String mydevicename= bluetoothAdapter.getName();
        //status= mydevicename+" : "+ mydeviceaddress;
        //Toast.makeText(this, status, Toast.LENGTH_LONG).show();


        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }


        //if (bluetoothAdapter.startDiscovery()==true){
        //Toast.makeText(this, "DISCOVERY STARTED", Toast.LENGTH_LONG).show();
        //}
        //bluetoothAdapter.startDiscovery();
        /////////////////////////////////////////
        /*
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();

         */

        sPref1 = getPreferences(MODE_PRIVATE);
        String savedText = sPref1.getString(SAVED_TEXT,null);
        if (savedText != null) {
            mCheckBox = findViewById(R.id.cb_remember_device_checkbox);
            mCheckBox.setVisibility(View.INVISIBLE);
            dest_mac = savedText;
            //Toast.makeText(getActivity(), dest_mac, Toast.LENGTH_LONG).show();
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }


        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView listView = findViewById(R.id.lv_listViewDeices);
                listView.setVisibility(View.VISIBLE);
                BondedList();
            }
        });

        btnDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView listView = findViewById(R.id.lv_listViewDeices);
                listView.setVisibility(View.VISIBLE);
                startDiscovery();
            }
        });
        btnCancelDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.cancelDiscovery();
            }
        });
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAcceptThread.cancel();
                }catch (NullPointerException e){

                }
                try {
                    mConnectedThread.cancel();
                } catch (NullPointerException e){

                }
                mAcceptThread=null;
                mConnectedThread=null;
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e){

                }

                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
            }
        });
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        sb.delete(0, sb.length());
                        //byte[] readBuf = (byte[]) msg.obj;
                        String readMessage = (String) msg.obj;
                        int message_len = readMessage.length();
                        if (message_len > 20){
                            int endPos = readMessage.indexOf("\n");
                            sb.append(readMessage.substring(endPos+1,endPos+1+12));
                        }
                        //String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        //Date currentDate = new Date();
                        //DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        //String timeText = timeFormat.format(currentDate);
                        sb.append(readMessage);                                                // append string
                        //sb.append("---");
                        //sb.append(timeText);
                        //message_bar.setText(sb);
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        //Toast.makeText(MainActivity.this, sb, Toast.LENGTH_LONG).show();
                        if (endOfLineIndex ==10) {                                            // if end-of-line,
                            //Toast.makeText(MainActivity.this, strIncom, Toast.LENGTH_LONG).show();
                            String temp = sb.substring(0 , 2);// extract string
                            String press = sb.substring(2 , 5);
                            String hum = sb.substring(5 , 7);
                            String avg_press = sb.substring(7, endOfLineIndex);
                            sb.delete(0, sb.length());                                      // and clear

                            if (forecast_flag == false && avg_press.equals("000")){
                                forecast.setText(R.string.wait_for_forecast);
                            }else if (forecast_flag == false && !avg_press.equals("000")){
                                SharedPreferences sPref_city = getSharedPreferences("pref", Context.MODE_PRIVATE);
                                String savedText = sPref_city.getString("city",null);
                                if (savedText == null) {
                                    forecast.setText(R.string.error_enter_and_save_city);
                                }else {
                                    prepare_forecast_data(press,avg_press,savedText);
                                    forecast_flag = true;
                                }
                            }

                            String mystring = getResources().getString(R.string.temperature) + temp + "°С";
                            message_bar.setText(mystring);            // update TextView
                            mystring = getResources().getString(R.string.pressure) + press + getResources().getString(R.string.mmHg);
                            pressure.setText(mystring);
                            mystring = getResources().getString(R.string.humidity) + hum + "%";
                            humidity.setText(mystring);

                            //btnOff.setEnabled(true);
                            //btnOn.setEnabled(true);
                        } else if (endOfLineIndex ==9) {
                            String temp = sb.substring(0 , 1);// extract string
                            String press = sb.substring(1 , 4);
                            String hum = sb.substring(4 , 6 );
                            String avg_press = sb.substring(6,endOfLineIndex);
                            sb.delete(0, sb.length());                                      // and clear
                            String mystring = getResources().getString(R.string.temperature) + temp + "°С";
                            message_bar.setText(mystring);            // update TextView
                            mystring = getResources().getString(R.string.pressure) + press + getResources().getString(R.string.mmHg);
                            pressure.setText(mystring);
                            mystring = getResources().getString(R.string.humidity) + hum + "%";
                            humidity.setText(mystring);

                        }

                        //sb.delete(0, sb.length());
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

    }


    private void BondedList() {
        mCheckBox = findViewById(R.id.cb_remember_device_checkbox);
        mCheckBox.setVisibility(View.VISIBLE);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ListView listView = findViewById(R.id.lv_listViewDeices);
        //Объявляем адаптер
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        //Ищем спаренные устройства
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // Если список спаренных устройств не пуст
        if (pairedDevices.size() > 0) {
            // идём в цикле по этому списку
            for (BluetoothDevice device : pairedDevices) {
                // Добавляем имена и адреса в mArrayAdapter,
                // чтобы показать через ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            // подключаем список к адаптеру
            listView.setAdapter(mArrayAdapter);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // по позиции получаем выбранный элемент
                    String selectedItem = mArrayAdapter.getItem(position);
                    // установка текста элемента TextView
                    dest_mac = selectedItem.substring(selectedItem.length() - 17, selectedItem.length());
                    saveMac();
                    //Toast.makeText(MainActivity.this, dest_mac, Toast.LENGTH_LONG).show();
                    mAcceptThread = new AcceptThread();
                    mAcceptThread.start();
                    ListView listView = findViewById(R.id.lv_listViewDeices);
                    listView.setVisibility(View.INVISIBLE);

                }
            });
        } else {
            message_bar.setText("List of BT devices is empty");
            //Toast.makeText(this, "List of BT devices is empty", Toast.LENGTH_LONG).show();
        }

        //btnPaired.setVisibility(View.INVISIBLE);
        //btnDiscovery.setVisibility(View.VISIBLE);

    }

    //private void DiscoveryList() {
    // startDiscovery();
    //}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // Make sure the request was successful
        //BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (resultCode == RESULT_OK) {
            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.
            //flag=true;
            message_bar.setText(R.string.bluetooth_enabled);
            message_bar.setVisibility(View.VISIBLE);
            sPref1 = getPreferences(MODE_PRIVATE);
            String savedText = sPref1.getString(SAVED_TEXT,null);
            if (savedText != null) {
                dest_mac = savedText;
                //Toast.makeText(getActivity(), dest_mac, Toast.LENGTH_LONG).show();
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
            }
        } else {
            message_bar.setText(R.string.bluetooth_cancelled);
            message_bar.setVisibility(View.VISIBLE);
        }

    }

    public void startDiscovery (){

        mCheckBox = findViewById(R.id.cb_remember_device_checkbox);
        mCheckBox.setVisibility(View.VISIBLE);
        //Объявляем адаптер
        //Toast.makeText(MainActivity.this, "ADAPTER DECLARATION", Toast.LENGTH_LONG).show();
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ListView listView = findViewById(R.id.lv_listViewDeices);

                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                    Toast.makeText(MainActivity.this, "Discovery started", Toast.LENGTH_SHORT).show();


                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismis progress dialog
                    if (mArrayAdapter.getCount() == 0){
                        Toast.makeText(MainActivity.this, "Discovery finished. NO DEVICES FOUND", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(MainActivity.this, "Discovery finished", Toast.LENGTH_SHORT).show();
                    }
                    unregisterReceiver(this);

                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.


                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());


                    //Toast.makeText(getApplicationContext(), "HA-HA1111", Toast.LENGTH_LONG).show();
                    //String deviceName = device.getName();
                    //Toast.makeText(MainActivity.this, "Found device " + deviceName, Toast.LENGTH_LONG).show();
                    //message_bar.setText(deviceName);
                    //message_bar.setVisibility(View.VISIBLE);
                    //String deviceHardwareAddress = device.getAddress(); // MAC address
                }

                //if (discoveryFinishFlag == true) {
                listView.setAdapter(mArrayAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        // по позиции получаем выбранный элемент
                        String selectedItem = mArrayAdapter.getItem(position);
                        // установка текста элемента TextView
                        dest_mac = selectedItem.substring(selectedItem.length() - 17, selectedItem.length());
                        saveMac();
                        //Toast.makeText(MainActivity.this, dest_mac, Toast.LENGTH_LONG).show();
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                        ListView listView = findViewById(R.id.lv_listViewDeices);
                        listView.setVisibility(View.INVISIBLE);

                    }
                });
                //}
            }
        };
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
        //btnDiscovery.setVisibility(View.INVISIBLE);
        //btnPaired.setVisibility(View.VISIBLE);

    }

    public void make_forecast(String wind){

        int press_trend = 1;
        //forecast.setText(wind + " " + global_press + " " + global_avg_press);
        Date currentDate = new Date();
        DateFormat timeFormat = new SimpleDateFormat("MM", Locale.getDefault());
        String monthText = timeFormat.format(currentDate);
        int monthInt = Integer.parseInt(monthText);

        double pressHPA = Double.parseDouble(global_press);
        pressHPA = pressHPA / 0.75006;

        pressHPA = wind_dir_correction(wind, pressHPA);

        if (Integer.parseInt(global_press) > Integer.parseInt(global_avg_press)){

            press_trend = 2; //grow
        }else if (Integer.parseInt(global_press) < Integer.parseInt(global_avg_press)){

            press_trend = 0; //fall
        }else if (Integer.parseInt(global_press) == Integer.parseInt(global_avg_press)){

            press_trend = 1; //stable
        }

        if (press_trend != 1 && monthInt >= 4 && monthInt<=9){
            if (press_trend == 2){

                pressHPA = pressHPA + 3.2;
            }else {

                pressHPA = pressHPA - 3.2;
            }
        }
        double z ;
        String letterForecast;
        if (press_trend == 2){
            z = 0.174 * (1031.4 - pressHPA);
            z = z + 0.5;
            //z = Math.round(z);
            letterForecast = grow_correction(z);
        }else if (press_trend == 0){
            z = 0.1553 * (1029.95 - pressHPA);
            z = z + 0.5;
            //z = Math.round(z);
            letterForecast = fall_correction(z);
        }else{
            z = 0.2314 * (1030.81 - pressHPA);
            z = z + 0.5;
            //z = Math.round(z);
            letterForecast = stable_correction(z);
        }

        //forecast.setText(letterForecast);
        //forecast.setText(String.valueOf(z));
        String stringForecast = forecastToString(letterForecast);

        forecast.setText(stringForecast);

        //int intPressHPA = (int) Math.round(pressHPA);
        
        //if (monthInt >= 4 && monthInt<=9){
            //forecast.setText("Summer");
        //}else{
            //forecast.setText("Winter");
        //}
    }
    public void prepare_forecast_data (String press, String avg_press, String saved_text){

        global_press = press;
        global_avg_press = avg_press;

        URL generatedUrl_SINOTIK = generateURL_SINOPTIK(saved_text);
        new getWindSINOPTIK().execute(generatedUrl_SINOTIK);
        
    }

    public double wind_dir_correction (String wind, double pressHPA){

        if (wind.equals("Північний")){
            pressHPA = pressHPA + 5.2;
        } else if (wind.equals("Північно-східний")){
            pressHPA = pressHPA + 3.2;
        } else if (wind.equals("Cхідний")){
            pressHPA = pressHPA - 1.1;
        } else if (wind.equals("Південно-східний")){
            pressHPA = pressHPA - 5.2;
        } else if (wind.equals("Південний")){
            pressHPA = pressHPA - 11.5;
        } else if (wind.equals("Південно-західний")){
            pressHPA = pressHPA - 7.3;
        } else if (wind.equals("Західний")){
            pressHPA = pressHPA - 3.2;
        } else if (wind.equals("Північно-західний")){
            pressHPA = pressHPA + 0.9;
        }
        return pressHPA;
    }

    public String grow_correction (double z){

         int intZ = (int) z;
         String letterForecast;
         if (intZ == 0){
             letterForecast = "A";
         } else if (intZ == 1 || intZ == 2){
             letterForecast = "B";
         } else if (intZ == 3){
             letterForecast = "C";
         } else if (intZ == 4){
             letterForecast = "F";
         } else if (intZ == 5){
             letterForecast = "G";
         } else if (intZ == 6){
             letterForecast = "I";
         } else if (intZ == 7){
             letterForecast = "J";
         } else if (intZ == 8){
             letterForecast = "L";
         } else if (intZ == 9 || intZ == 10){
             letterForecast = "M";
         } else if (intZ == 11){
             letterForecast = "Q";
         } else if (intZ == 12){
             letterForecast = "T";
         } else if (intZ == 13){
             letterForecast = "Y";
         } else {

             letterForecast = "ERROR GROW";
         }

        return letterForecast;

    }

    public String fall_correction (double z){

        int intZ = (int) z;
        String letterForecast;
        if (intZ == 0){
            letterForecast = "B";
        } else if (intZ == 1){
            letterForecast = "D";
        } else if (intZ == 2){
            letterForecast = "H";
        } else if (intZ == 3){
            letterForecast = "O";
        } else if (intZ == 4){
            letterForecast = "R";
        } else if (intZ == 5){
            letterForecast = "U";
        } else if (intZ == 6){
            letterForecast = "V";
        } else if (intZ == 7 || intZ == 8){
            letterForecast = "X";
        } else if (intZ == 9 || intZ == 10 || intZ == 11){  //added 10 and 11, but it wasn't in algorythm. Calculations gived z=10.9. Maybe because of inacuracy middle value of press
            letterForecast = "Z";
        } else {

            letterForecast = "ERROR FALL";
        }

        return letterForecast;

    }

    public String stable_correction (double z){
        int intZ = (int) z;
        String letterForecast;
        if (intZ == 0){
            letterForecast = "A";
        } else if (intZ == 1 || intZ == 2 || intZ == 3){
            letterForecast = "B";
        } else if (intZ == 4){
            letterForecast = "E";
        } else if (intZ == 5){
            letterForecast = "K";
        } else if (intZ == 6 || intZ == 7){
            letterForecast = "N";
        } else if (intZ == 8 || intZ == 9){
            letterForecast = "P";
        } else if (intZ == 10){
            letterForecast = "S";
        } else if (intZ == 11 || intZ == 12){
            letterForecast = "W";
        } else if (intZ == 13 || intZ == 14 || intZ == 15){
            letterForecast = "X";
        } else if (intZ == 16 || intZ == 17){
            letterForecast = "Z";
        } else {

            letterForecast = "ERROR STABLE";
        }

        return letterForecast;

    }

    public String forecastToString (String letterForecast){

        String returnString = "???";

        if (letterForecast.equals("A")){

            returnString = "Відмінна, ясно";
        } else if (letterForecast.equals("B")){

            returnString = "Хороша, ясно";
        } else if (letterForecast.equals("C")){

            returnString = "Встановлення хорошої погоди";
        } else if (letterForecast.equals("D")){

            returnString = "Хороша, погіршується";
        } else if (letterForecast.equals("E")){

            returnString = "Хороша, можлива злива";
        } else if (letterForecast.equals("F")){

            returnString = "Достатньо хороша, покращується";
        } else if (letterForecast.equals("G")){

            returnString = "Достатньо хороша, можлива злива";
        } else if (letterForecast.equals("H")){

            returnString = "Достатньо хороша, очікується злива";
        } else if (letterForecast.equals("I")){

            returnString = "Злива, покращується";
        } else if (letterForecast.equals("J")){

            returnString = "Мінлива, покращується";
        } else if (letterForecast.equals("K")){

            returnString = "Достатньо хороша, можливі зливи";
        } else if (letterForecast.equals("L")){

            returnString = "Похмуро, згодом проясниться";
        } else if (letterForecast.equals("M")){

            returnString = "Похмуро, можливе покращення";
        } else if (letterForecast.equals("N")){

            returnString = "Зливи, можливе покращення";
        } else if (letterForecast.equals("O")){

            returnString = "Зливи, стає нестабільною";
        } else if (letterForecast.equals("P")){

            returnString = "Мінлива, невеликі дощі";
        } else if (letterForecast.equals("Q")){

            returnString = "Похмура, короткі прояснення";
        } else if (letterForecast.equals("R")){

            returnString = "Похмура, очікуються дощі";
        } else if (letterForecast.equals("S")){

            returnString = "Похмура, часом дощі";
        } else if (letterForecast.equals("T")){

            returnString = "Переважно дуже похмура";
        } else if (letterForecast.equals("U")){

            returnString = "Місцями дощ, погіршення";
        } else if (letterForecast.equals("V")){

            returnString = "Місцями дощ, дуже погана, похмуро";
        } else if (letterForecast.equals("W")){

            returnString = "Часті дощі";
        } else if (letterForecast.equals("X")){

            returnString = "Дощі, дуже погана, похмуро";
        } else if (letterForecast.equals("Y")){

            returnString = "Штормова, йде на покращення";
        } else if (letterForecast.equals("Z")){

            returnString = "Штормова, дощі";
        } else if (letterForecast.equals("ERROR GROW")){

            returnString = "ERROR GROW";
        } else if (letterForecast.equals("ERROR FALL")){

            returnString = "ERROR FALL";
        }else if (letterForecast.equals("ERROR STABLE")){

            returnString = "ERROR STABLE";
        }

        return returnString;
    }
    //////////////////////////////
    /*
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Toast.makeText(MainActivity.this, "HA-HA11112", Toast.LENGTH_LONG).show();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                Toast.makeText(MainActivity.this, "Discovery started", Toast.LENGTH_LONG).show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                Toast.makeText(MainActivity.this, "Discovery finished", Toast.LENGTH_LONG).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                //Toast.makeText(MainActivity.this, "HA-HA11112", Toast.LENGTH_LONG).show();
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //showToast("Found device " + device.getName());
                Toast.makeText(MainActivity.this, "Found device " + device.getName(), Toast.LENGTH_LONG).show();

            }
        }
    };

     */

    @Override
    public void onDestroy() {
        //getActivity().unregisterReceiver(receiver);
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.

    }

    @Override
    public void onPause(){
        //getActivity().unregisterReceiver(receiver);
        super.onPause();
        //Toast.makeText(getActivity(), "onPause", Toast.LENGTH_SHORT).show();
        saveMac();
    }
    @Override
    public void onResume(){
        super.onResume();
    }

    //public void onSaveInstanceState(Bundle outState) {
    //    super.onSaveInstanceState(outState);
        //saveMac();
   // }

    public void saveMac (){
        mCheckBox = findViewById(R.id.cb_remember_device_checkbox);
        if(mCheckBox.isChecked()){
            sPref1 = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref1.edit();
            ed.putString(SAVED_TEXT, dest_mac);
            ed.commit();

        }
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        public AcceptThread() {


            // используем вспомогательную переменную, которую в дальнейшем
            // свяжем с mmServerSocket,

            BluetoothServerSocket tmp = null;

            message_bar.setText(R.string.connecting_message);
            try {
                // MY_UUID это UUID нашего приложения, это же значение
                // используется в клиентском приложении

                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("bluetotthconnect", MY_UUID);
            } catch (IOException e) {

                Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Socket's listen() method failed", Toast.LENGTH_SHORT).show();
                    }
                }, 0);
            }


            mmServerSocket = tmp;


        }


        public void run() {

            BluetoothSocket socket;

            // ждем пока не произойдет ошибка или не
            // будет возвращен сокет
            while (true) {
                try {
                    //runOnUiThread(new Runnable() {
                    //  public void run() {

                    //Toast.makeText(MainActivity.this, "try to connect", Toast.LENGTH_LONG).show();
                    //}
                    //});
                    bluetoothAdapter.cancelDiscovery();
                    String address = dest_mac;
                    BluetoothDevice remote_dev = bluetoothAdapter.getRemoteDevice(address);
                    socket = remote_dev.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    socket.connect();
                    //runOnUiThread(new Runnable() {
                    //  public void run() {

                    // Toast.makeText(MainActivity.this, "connect() finished", Toast.LENGTH_LONG).show();
                    //}
                    //});
                } catch (IOException e) {

                    runOnUiThread(new Runnable() {
                        public void run() {

                            message_bar.setText(R.string.connect_method_failed);
                            Toast.makeText(MainActivity.this, "connect() method failed", Toast.LENGTH_LONG).show();
                        }
                    });

                    break;
                }
                // если соединение было подтверждено
                if (socket == null) {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            message_bar.setText(R.string.socket_null);
                            Toast.makeText(MainActivity.this, "socket==null", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                if (socket != null) {
                    // управлчем соединением (в отдельном потоке)
                    //manageConnectedSocket(socket);
                    mConnectedThread = new ConnectedThread(socket);
                    mConnectedThread.start();

                    //runOnUiThread(new Runnable() {
                    //  public void run() {

                    //  Toast.makeText(MainActivity.this, "connection established successfully", Toast.LENGTH_LONG).show();
                    //}
                    //});

                    /*
                    try {
                        //mmServerSocket.close();
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(MainActivity.this, "socket closed", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(MainActivity.this, "could not close socket", Toast.LENGTH_LONG).show();
                            }
                        });
                    }*/

                    break;
                }
            }


        }


        /**
         * отмена ожидания сокета
         */
        public void cancel() {
            try {
                mmServerSocket.close();
                //mConnectedThread.cancel();


            } catch (IOException | NullPointerException e) {

            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {

                runOnUiThread(new Runnable() {
                    public void run() {

                        message_bar.setText(R.string.error_input_stream);
                        Toast.makeText(MainActivity.this, "Error occurred when creating input stream", Toast.LENGTH_LONG).show();
                    }
                });
            }

             /*
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(MainActivity.this, "Error occurred when creating output stream", Toast.LENGTH_LONG).show();
                    }
                });

            }*/

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes;
            // bytes returned from read()
            StringBuilder readMessage = new StringBuilder();
            // Keep listening to the InputStream until an exception occurs.
            while (true) {

                try {
                    numBytes = mmInStream.read(mmBuffer);
                    String read = new String(mmBuffer, 0, numBytes);
                    readMessage.append(read);

                    if (read.contains("\n")) {
                        //Date currentDate = new Date();
                        //DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        //String timeText = timeFormat.format(currentDate);
                        h.obtainMessage(RECIEVE_MESSAGE, numBytes, -1, readMessage.toString()).sendToTarget();
                        readMessage.setLength(0);
                    }
                    // Read from the InputStream.
                    /*
                    numBytes = mmInStream.read(mmBuffer);
                    //h.obtainMessage(RECIEVE_MESSAGE, numBytes, -1, mmBuffer).sendToTarget();
                    Message readMsg = h.obtainMessage(
                            RECIEVE_MESSAGE, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();

                     */
                    //runOnUiThread(new Runnable() {
                    //  public void run() {

                    //Toast.makeText(MainActivity.this, numBytes, Toast.LENGTH_LONG).show();
                    //}
                    //});
                    // Send the obtained bytes to the UI activity.
                    //Message readMsg = handler.obtainMessage(
                    //       MessageConstants.MESSAGE_READ, numBytes, -1,
                    //     mmBuffer);
                    //readMsg.sendToTarget();
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            message_bar.setText(R.string.input_stream_disconnected);
                            //Toast.makeText(getActivity(), "Input stream was disconnected", Toast.LENGTH_LONG).show();
                        }
                    });

                    break;
                }
            }

        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (NullPointerException | IOException e) {
                //Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}