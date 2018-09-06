package io.hops.android.center;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.hops.android.streams.hopsworks.AckRecordDTO;
import io.hops.android.streams.hopsworks.DeviceDTO;
import io.hops.android.streams.hopsworks.HopsWorksClient;
import io.hops.android.streams.hopsworks.HopsWorksClientBuilder;
import io.hops.android.streams.hopsworks.HopsWorksResponse;
import io.hops.android.streams.hopsworks.SchemaDTO;
import io.hops.android.streams.hopsworks.TopicRecordsDTO;
import io.hops.android.streams.records.AvroTemplate;
import io.hops.android.streams.records.IntrabodyRecord;
import io.hops.android.streams.records.Record;
import io.hops.android.streams.storage.DeviceCredentials;
import io.hops.android.streams.storage.PropertiesTable;
import io.hops.android.streams.storage.RecordsTable;
import io.hops.android.streams.storage.SQLite;
import io.hops.android.streams.storage.SQLiteNotInitialized;
import io.hops.android.streams.streams.RecordStreamWorker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity{

    private static String BASE_URL = "https://bbc2.sics.se:8080";
    private HopsWorksClient hopsworksClient;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private long recordsReceived = 0;
    //Bluetooth variables
    private BluetoothAdapter mBTAdapter;
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private final String TAG = MainActivity.class.getSimpleName();
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"/*"00001101-0000-1000-8000-00805F9B34FA"*/); // "random" unique identifier

    private final static int UPDATE_DISPLAY = 1;
    private final static int UPDATE_DISPLAY_REGISTER = 2;
    private final static int RECORD_RECEIVED = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        ((EditText) findViewById(R.id.txtServer)).setText("https://bbc2.sics.se:8080");
        BASE_URL = BASE_URL + "/hopsworks-api/api/devices-api/";
        hopsworksClient = HopsWorksClientBuilder.getHopsWorksClientSkipSslChecks(BASE_URL);
        final TextView txtDisplay = (TextView)findViewById(R.id.txtDisplay);
        txtDisplay.setMovementMethod(new ScrollingMovementMethod());
        final TextView txtDisplayRegister = (TextView)findViewById(R.id.txtDisplayRegister);
        txtDisplayRegister.setMovementMethod(new ScrollingMovementMethod());
        SQLite.init(this.getApplicationContext());
        setRecordsView();

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == UPDATE_DISPLAY) {
                    String readMessage = String.valueOf(msg.obj);
                    if(readMessage!= null && !readMessage.isEmpty()) {
                        System.out.println("UPDATE_DISPLAY:" + readMessage);
                        getDisplay(R.id.txtDisplay).post(new DisplayTask(readMessage));
                    }
                }
                if (msg.what == UPDATE_DISPLAY_REGISTER) {
                    String readMessage = String.valueOf(msg.obj);
                    if(readMessage!= null && !readMessage.isEmpty()) {
                        System.out.println("UPDATE_DISPLAY_REGISTER:" + readMessage);
                        getDisplay(R.id.txtDisplayRegister).post(new DisplayTask(readMessage));
                    }
                }
                if (msg.what == RECORD_RECEIVED) {
                    System.out.println("Hopsworks :: RECORD_RECEIVED-"+recordsReceived);
                    recordsReceived++;
                    setRecordsView();
                }
            }
        };

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        if(!mBTAdapter.isEnabled()){
            Toast.makeText(MainActivity.this,
                    "Please turn on Bluetooth and make your device discoverable!",
                    Toast.LENGTH_LONG).show();
        }

        //Start Bluetooth server
        // Spawn a new thread to avoid blocking the GUI one
        AcceptThread accept = new AcceptThread();
        accept.start();
    }

    private void display(int displayId, String text){
        Log.i(MainActivity.class.getSimpleName(), text);
        //Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        final TextView txtDisplay = (TextView)findViewById(displayId);
        txtDisplay.setText(txtDisplay.getText() + "\n" + text);
    }

    private void display(int displayId, HopsWorksResponse response){
        if (response == null || response.getCode() == null){
            display(displayId,"Unexpected error.");
        }else{
            if (response.getCode() >= 200 && response.getCode() < 300){
                display(displayId, String.valueOf(response.getCode()) + " " + response.getMessage());
            }else{
                display(displayId, String.valueOf(response.getCode()) + " " + response.getReason());
            }
        }

    }

    private TextView getDisplay(int displayId){
        return (TextView)findViewById(displayId);
    }

    private String getProjectName() {
        return ((TextView) findViewById(R.id.txtProjectName)).getText().toString().trim();
    }

    private String getProjectTopic(){
        return ((TextView) findViewById(R.id.txtTopicName)).getText().toString().trim();
    }

    private String getAlias(){
        return ((TextView) findViewById(R.id.txtAlias)).getText().toString().trim();
    }

    private String getServer(){
        return ((EditText) findViewById(R.id.txtServer)).getText().toString().trim();
    }

    private void setRecordsView(){
        ((TextView) findViewById(R.id.lblRecordsCount)).setText(Long.toString(recordsReceived));
    }

    private void setJwtToken(String jwtToken){
        try {
            PropertiesTable.write("jwt", jwtToken);
        } catch (SQLiteNotInitialized sqLiteNotInitialized) {
            display(R.id.txtDisplayRegister, "Jwt could not be saved.");
        }
    }

    private String getJwtToken(){
        try {
            return "Bearer " + PropertiesTable.read("jwt");
        } catch (SQLiteNotInitialized sqLiteNotInitialized) {
            display(R.id.txtDisplayRegister, "No jwt token available.");
            return null;
        }
    }

    private DeviceDTO getDeviceInfo(){
        SQLite.init(this.getApplicationContext());
        try{
            return new DeviceDTO(
                    DeviceCredentials.getDeviceUUID(), DeviceCredentials.getPassword(), getAlias());
        }catch (SQLiteNotInitialized SQLiteNotInitialized){
            display(R.id.txtDisplayRegister, SQLiteNotInitialized.getMessage());
            return null;
        }
    }

    /***
     * Code executed when the Register button is clicked.
     */
    public void register(View view) {
        display(R.id.txtDisplayRegister, "Register clicked.");
        Call<HopsWorksResponse> call = hopsworksClient.register(getProjectName(), getDeviceInfo());
        call.enqueue(new Callback<HopsWorksResponse>() {
            @Override
            public void onResponse(
                    Call<HopsWorksResponse> call, Response<HopsWorksResponse> response) {
                if (response.isSuccessful()){
                    display(R.id.txtDisplayRegister, response.body());
                }else{
                    try {
                        display(R.id.txtDisplayRegister, HopsWorksResponse.fromJson(response.errorBody().string()));
                    } catch (IOException e) {
                        display(R.id.txtDisplayRegister, e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<HopsWorksResponse> call, Throwable t) {
                display(R.id.txtDisplayRegister, t.getMessage());
            }
        });
    }

    /***
     * Code executed when the Login button is clicked.
     */
    public void login(View view) {
        display(R.id.txtDisplayRegister, "Login clicked.");
        Call<HopsWorksResponse> call = hopsworksClient.login(getProjectName(),  getDeviceInfo());
        call.enqueue(new Callback<HopsWorksResponse>() {
            @Override
            public void onResponse(
                    Call<HopsWorksResponse> call, Response<HopsWorksResponse> response) {
                if (response.isSuccessful()){
                    display(R.id.txtDisplayRegister, response.body());
                    setJwtToken(response.body().getJwt());
                }else{
                    try {
                        display(R.id.txtDisplayRegister, HopsWorksResponse.fromJson(response.errorBody().string()));
                    } catch (IOException e) {
                        display(R.id.txtDisplayRegister, e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<HopsWorksResponse> call, Throwable t) {
                display(R.id.txtDisplayRegister, t.getMessage());
            }
        });
    }

    /***
     * Code executed when the Verify Token button is clicked.
     */
    public void verify(View view) {
        display(R.id.txtDisplayRegister, "Verify Token clicked.");
        Call<HopsWorksResponse> call = hopsworksClient.verifyToken(
                getProjectName(), getJwtToken(), getDeviceInfo());
        call.enqueue(new Callback<HopsWorksResponse>() {
            @Override
            public void onResponse(
                    Call<HopsWorksResponse> call, Response<HopsWorksResponse> response) {
                if (response.isSuccessful()){
                    display(R.id.txtDisplayRegister, response.body());
                }else{
                    try {
                        display(R.id.txtDisplayRegister, HopsWorksResponse.fromJson(response.errorBody().string()));
                    } catch (IOException e) {
                        display(R.id.txtDisplayRegister, e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<HopsWorksResponse> call, Throwable t) {
                display(R.id.txtDisplayRegister, t.getMessage());
            }
        });
    }

    /***
     * Code executed when the Get Schema button is clicked.
     */
    public void getSchema(View view) {
        display(R.id.txtDisplayRegister, "Get Schema clicked.");
        Call<SchemaDTO> call = hopsworksClient.getTopicSchema(
                getProjectName(), getJwtToken(), getProjectTopic());
        call.enqueue(new Callback<SchemaDTO>() {
            @Override
            public void onResponse(
                    Call<SchemaDTO> call, Response<SchemaDTO> response) {
                try {
                    if (response.isSuccessful()){
                        display(R.id.txtDisplayRegister, "Get Schema was successful!");
                        display(R.id.txtDisplayRegister, response.body().getContents());
                    }else{
                        display(R.id.txtDisplayRegister, HopsWorksResponse.fromJson(
                                response.errorBody().string()).getReason());
                    }
                } catch (IOException e) {
                    display(R.id.txtDisplayRegister, e.getMessage());
                }
            }
            @Override
            public void onFailure(Call<SchemaDTO> call, Throwable t) {
                display(R.id.txtDisplayRegister, "Get Schema failed! Reason: " + t.getMessage());
            }
        });
    }

    /***
     * Code executed when the Produce button is clicked.
     */
    public void produce(View view) {
        display(R.id.txtDisplay, "Produce clicked.");
        try {
            // A Record is instantly generated but not saved in the storage.
            Random randomGenerator = new Random();
            IntrabodyRecord record = new IntrabodyRecord("{dvcId: 18367, channelId: 123, value: 4507988.0, unit: 5, time: 1521560115768}");
            final ArrayList<Record> listRecords = new ArrayList<Record>();
            listRecords.add(record);

            // Creates a call object.
            Call<List<AckRecordDTO>> call = hopsworksClient.produce(getProjectName(), getJwtToken(),
                    new TopicRecordsDTO(getProjectTopic(), listRecords));

            // Call is made.
            call.enqueue(new Callback<List<AckRecordDTO>>() {
                @Override
                public void onResponse(
                        Call<List<AckRecordDTO>> call, Response<List<AckRecordDTO>> response) {
                    try {
                        if (response.isSuccessful()){
                            List<AckRecordDTO> records = response.body();
                            if (listRecords.size() != records.size()){
                                display(R.id.txtDisplay, "Length mismatch!");
                            }else{
                                display(R.id.txtDisplay, String.valueOf(records.get(0).isAck()));
                            }
                        }else{
                            display(R.id.txtDisplay, HopsWorksResponse.fromJson(
                                    response.errorBody().string()).getReason());
                        }
                    } catch (IOException e) {
                        display(R.id.txtDisplay, e.getMessage());
                    }
                }
                @Override
                public void onFailure(Call<List<AckRecordDTO>> call, Throwable t) {
                    display(R.id.txtDisplay, "Produce failed! Reason: " + t.getMessage());
                }
            });
        } catch (SQLiteNotInitialized sqliteNotInitialized) {
            display(R.id.txtDisplay, sqliteNotInitialized.getMessage());
        } catch (JSONException e) {
            display(R.id.txtDisplay, e.getMessage());
        }
    }

    /***
     * Code executed when the Stream button is clicked.
     */
    public void streamInBackground(View view) {
        RecordStreamWorker recordStreamWorker =
                RecordStreamWorker.getInstance(IntrabodyRecord.class);
        if (recordStreamWorker.isStreaming()){
            recordStreamWorker.stopTimeSyncing();
            recordStreamWorker.stopProducing();
            recordStreamWorker.stopStreaming();
            recordStreamWorker.stopCleaning();
            display(R.id.txtDisplay, "Thread ended");
            Button btnStream = (Button)findViewById(R.id.btnProduceInBackground);
            btnStream.setText("Start Stream");
        }else{
            display(R.id.txtDisplay, "Thread started");
            recordStreamWorker.timeSync();
            recordStreamWorker.clean();
            //recordStreamWorker.produce(new ProduceTask(), 0, 4, TimeUnit.SECONDS);
            recordStreamWorker.stream(
                    new StreamTask(this.getApplicationContext()), 0, 10, TimeUnit.SECONDS);
            Button btnStream = (Button)findViewById(R.id.btnProduceInBackground);
            btnStream.setText("Stop Stream");
        }
    }

    public static void closeStreaming(Class cls){
        RecordStreamWorker recordStreamWorker = RecordStreamWorker.getInstance(cls);
        recordStreamWorker.close();
    }


    private class DisplayTask implements Runnable{

        private String message;

        DisplayTask(String message){
            this.message = message;
        }
        public void run() {
            display(R.id.txtDisplay, message);
        }
    }

    /**
     * Generates a record and saves it in the SQLite database.
     */
    private class ProduceTask implements Runnable{

        @Override
        public void run() {
            try {
                Random randomGenerator = new Random();
                IntrabodyRecord record = new IntrabodyRecord("{dvcId: 18367, channelId: 123, value: 4507988.0, unit: 5, time: 1521560115768}");

                record.save();
                getDisplay(R.id.txtDisplay).post(new DisplayTask(record.getRecordUUID() + " pro"));
            } catch (SQLiteNotInitialized SQLiteNotInitialized) {
                display(R.id.txtDisplay, SQLiteNotInitialized.getMessage());
            } catch (JSONException e) {
                display(R.id.txtDisplay, e.getMessage());
            }
        }
    }

    /**
     * Gets records that are not acknowledged and streams them to HopsWorks.
     */
    private class StreamTask implements Runnable{

        private Context context;

        public StreamTask(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            try {
                // Gets the records that are not acknowledged from the database.
                final ArrayList<Record> records =
                        RecordsTable.readAllNotAcked(IntrabodyRecord.class);
                if (records.isEmpty()){
                    getDisplay(R.id.txtDisplay).post(new DisplayTask("No records to send."));
                    return;
                }else if(!NetworkUtils.isNetworkAvailable(context)){
                    getDisplay(R.id.txtDisplay).post(new DisplayTask("No internet connection."));
                    return;
                }
                Call<List<AckRecordDTO>> call = hopsworksClient.produce(
                        getProjectName(),
                        getJwtToken(),
                        new TopicRecordsDTO(getProjectTopic(), records)
                );

                call.enqueue(new Callback<List<AckRecordDTO>>() {
                    @Override
                    public void onResponse(
                            Call<List<AckRecordDTO>> call, Response<List<AckRecordDTO>> response) {
                        try {
                            if (response.isSuccessful()) {
                                List<AckRecordDTO> acks = response.body();
                                if (records.size() != acks.size()) {
                                    getDisplay(R.id.txtDisplay).post(new DisplayTask("Length Miss match."));
                                } else {
                                    for (int i = 0; i < records.size(); i++) {
                                        Record record = records.get(i);
                                        if (acks.get(i).isAck()){
                                            record.setAcked(true);
                                            // Acknowledgement of records is saved in the db.
                                            record.save();
                                            // Alternatively, you can delete with record.delete()
                                            getDisplay(R.id.txtDisplay).post(
                                                    new DisplayTask(
                                                            record.getRecordUUID() + " ack"));
                                        }
                                    }
                                }
                            } else {
                                getDisplay(R.id.txtDisplay).post(
                                        new DisplayTask(HopsWorksResponse.fromJson(
                                                response.errorBody().string()).getReason()));
                            }
                        } catch (IOException e) {
                            display(R.id.txtDisplay, e.getMessage());
                        } catch (SQLiteNotInitialized SQLiteNotInitialized) {
                            display(R.id.txtDisplay, SQLiteNotInitialized.getMessage());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<AckRecordDTO>> call, Throwable t) {
                        display(R.id.txtDisplay, "Produce failed! Reason: " + t.getMessage());
                    }
                });
            } catch (SQLiteNotInitialized SQLiteNotInitialized) {
                SQLiteNotInitialized.printStackTrace();
            }
        }
    }

    private class AcceptThread extends Thread {
        // The local server socket
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null ;
            // Create a new listening server socket
            try{
                tmp = mBTAdapter.listenUsingInsecureRfcommWithServiceRecord("intrabody-android", BTMODULEUUID );
                getDisplay(R.id.txtDisplay).post(new DisplayTask("Bluetooth server running at: "+mBTAdapter.getAddress()));
                Log.d(TAG, "AcceptThread: Setting up Server using: " + BTMODULEUUID);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }
            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;
            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
                mHandler.obtainMessage(UPDATE_DISPLAY, "Bluetooth thread accepted connection from :"
                        +socket.getRemoteDevice().getAddress()).sendToTarget();
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
                mHandler.obtainMessage(UPDATE_DISPLAY, "Bluetooth thread error:"+e.getMessage()).sendToTarget();
            }

            if(socket != null){
                mConnectedThread = new ConnectedThread(socket);
                mConnectedThread.start();
            }
            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BufferedReader mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            BufferedReader tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                System.out.println("Initializing stream");
                tmpIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            try {
                String line;
                while ((line = mmInStream.readLine()) != null && !line.isEmpty() && !line.equalsIgnoreCase("null")) {
                    // Read from the InputStream
                    SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                                        //Create record and persist it to local storage SQLLite. Stream thread
                    //will pick it up and send it to Hopsworks
                    System.out.println("Hopsworks :: line-"+line);
                    mHandler.obtainMessage(UPDATE_DISPLAY, "message:"+line).sendToTarget();
                    IntrabodyRecord record = new IntrabodyRecord(line);
                    System.out.println("Hopsworks :: Intrabody.record-"+record);
                    record.save();
                    System.out.println("Hopsworks ::record-"+record + "  received via Bluetooth");
                    String byte1 = "11010000";
                    String byte2 = "00000000";
                    String byte3 = "01010101";
                    mmOutStream.write((byte1+byte2+byte3+"\r\n").getBytes());
                    mmOutStream.flush();
                    mHandler.obtainMessage(UPDATE_DISPLAY, "send ion-pump data to hardware").sendToTarget();
                    mHandler.obtainMessage(UPDATE_DISPLAY, "byte1:"+byte1).sendToTarget();
                    mHandler.obtainMessage(UPDATE_DISPLAY, "byte2:"+byte2).sendToTarget();
                    mHandler.obtainMessage(UPDATE_DISPLAY, "byte3:"+byte3).sendToTarget();

                    //mHandler.obtainMessage(UPDATE_DISPLAY, " Bluetooth rcv record:"+record.getRecordUUID()).sendToTarget();
                    mHandler.obtainMessage(RECORD_RECEIVED, "").sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLiteNotInitialized e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
                mmOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
