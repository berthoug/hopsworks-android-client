package io.hops.android.center;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.hops.android.streams.hopsworks.AckRecordDTO;
import io.hops.android.streams.hopsworks.DeviceDTO;
import io.hops.android.streams.hopsworks.HopsWorksClient;
import io.hops.android.streams.hopsworks.HopsWorksClientBuilder;
import io.hops.android.streams.hopsworks.HopsWorksResponse;
import io.hops.android.streams.hopsworks.SchemaDTO;
import io.hops.android.streams.hopsworks.TopicRecordsDTO;
import io.hops.android.streams.records.AvroTemplate;
import io.hops.android.streams.records.CoordinatesRecord;
import io.hops.android.streams.records.Record;
import io.hops.android.streams.storage.DeviceCredentials;
import io.hops.android.streams.storage.RecordsTable;
import io.hops.android.streams.storage.SQLite;
import io.hops.android.streams.storage.SQLiteNotInitialized;
import io.hops.android.streams.streams.RecordStreamWorker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity{

    private static final String BASE_URL =
            "https://bbc2.sics.se:8080/hopsworks-api/api/devices-api/";

    private HopsWorksClient hopsworksClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        hopsworksClient = HopsWorksClientBuilder.getHopsWorksClientSkipSslChecks(BASE_URL);
        final TextView txtDisplay = (TextView)findViewById(R.id.txtDisplay);
        txtDisplay.setMovementMethod(new ScrollingMovementMethod());
        SQLite.init(this.getApplicationContext());
    }

    @Override
    protected void onPause(){
        super.onPause();
        NotificationManager n =
                (NotificationManager) this.getApplicationContext().getSystemService(
                        Context.NOTIFICATION_SERVICE);
        n.cancel(1);
    }

    private void display(String text){
        Log.i(MainActivity.class.getSimpleName(), text);
        //Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        final TextView txtDisplay = (TextView)findViewById(R.id.txtDisplay);
        txtDisplay.setText(txtDisplay.getText() + "\n" + text);
    }

    private void display(HopsWorksResponse response){
        if (response == null || response.getCode() == null){
            display("Unexpected error.");
        }else{
            if (response.getCode() >= 200 && response.getCode() < 300){
                display(String.valueOf(response.getCode()) + " " + response.getMessage());
            }else{
                display(String.valueOf(response.getCode()) + " " + response.getReason());
            }
        }

    }

    private TextView getDisplay(){
        return (TextView)findViewById(R.id.txtDisplay);
    }

    private String getProjectName() {
        return ((TextView) findViewById(R.id.txtProjectName)).getText().toString();
    }

    private String getProjectTopic(){
        return ((TextView) findViewById(R.id.txtTopicName)).getText().toString();
    }

    private String getAlias(){
        return ((TextView) findViewById(R.id.txtAlias)).getText().toString();
    }

    private String getJwtToken(){
        return "Bearer " + ((TextView) findViewById(R.id.txtToken)).getText().toString();
    }

    private DeviceDTO getDeviceInfo(){
        SQLite.init(this.getApplicationContext());
        try{
            return new DeviceDTO(
                    DeviceCredentials.getDeviceUUID(),
                    DeviceCredentials.getPassword(),
                    getAlias());
        }catch (SQLiteNotInitialized SQLiteNotInitialized){
            display(SQLiteNotInitialized.getMessage());
            return null;
        }
    }

    public void register(View view) {
        display("Register clicked.");
        Call<HopsWorksResponse> call = hopsworksClient.register(getProjectName(), getDeviceInfo());
        call.enqueue(new Callback<HopsWorksResponse>() {
            @Override
            public void onResponse(
                    Call<HopsWorksResponse> call, Response<HopsWorksResponse> response) {
                if (response.isSuccessful()){
                    // Positive Response
                    display(response.body());
                }else{
                    // Negative Response
                    try {
                        display(HopsWorksResponse.fromJson(response.errorBody().string()));
                    } catch (IOException e) {
                        display(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<HopsWorksResponse> call, Throwable t) {
                // No Response
                display(t.getMessage());
            }
        });
    }

    public void login(View view) {
        display("Login clicked.");
        Call<HopsWorksResponse> call = hopsworksClient.login(getProjectName(),  getDeviceInfo());
        call.enqueue(new Callback<HopsWorksResponse>() {
            @Override
            public void onResponse(
                    Call<HopsWorksResponse> call, Response<HopsWorksResponse> response) {
                if (response.isSuccessful()){
                    // Positive Response
                    display(response.body());
                    final TextView txtToken = (TextView)findViewById(R.id.txtToken);
                    txtToken.setText(response.body().getJwt());
                }else{
                    // Negative Response
                    try {
                        display(HopsWorksResponse.fromJson(response.errorBody().string()));
                    } catch (IOException e) {
                        display(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<HopsWorksResponse> call, Throwable t) {
                // No Response
                display(t.getMessage());
            }
        });
    }

    public void verify(View view) {
        display("Verify Token clicked.");
        Call<HopsWorksResponse> call = hopsworksClient.verifyToken(
                getProjectName(), getJwtToken(), getDeviceInfo());
        call.enqueue(new Callback<HopsWorksResponse>() {
            @Override
            public void onResponse(
                    Call<HopsWorksResponse> call, Response<HopsWorksResponse> response) {
                if (response.isSuccessful()){
                    // Positive Response
                    display(response.body());
                }else{
                    // Negative Response
                    try {
                        display(HopsWorksResponse.fromJson(response.errorBody().string()));
                    } catch (IOException e) {
                        display(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<HopsWorksResponse> call, Throwable t) {
                // No Response
                display(t.getMessage());
            }
        });
    }

    public void produce(View view) {
        display("Produce clicked.");
        final List<Record> list = new ArrayList<>();
        try {
            Random randomGenerator = new Random();

            for (int i=0;i<1;i++) {
                CoordinatesRecord record = new CoordinatesRecord(
                        randomGenerator.nextDouble()*180, randomGenerator.nextDouble()*180);
                list.add(record);
            }
        } catch (SQLiteNotInitialized SQLiteNotInitialized) {
            SQLiteNotInitialized.printStackTrace();
        }
        final long y = SystemClock.currentThreadTimeMillis();

        Call<List<AckRecordDTO>> call = hopsworksClient.produce(
                getProjectName(), getJwtToken(), new TopicRecordsDTO(getProjectTopic(), list));

        call.enqueue(new Callback<List<AckRecordDTO>>() {
            @Override
            public void onResponse(
                    Call<List<AckRecordDTO>> call, Response<List<AckRecordDTO>> response) {
                try {
                    if (response.isSuccessful()){
                        final long z = SystemClock.currentThreadTimeMillis();
                        List<AckRecordDTO> records = response.body();

                        if (list.size() != records.size()){
                            display("Length miss match!");
                        }else{
                            for (int i=0; i<list.size();i++){
                                list.get(i).setAcked(true);
                            }
                            display(String.valueOf(records.get(0).isAck()));
                        }
                        display("Network Time: "+String.valueOf((z-y)));
                    }else{
                        display(HopsWorksResponse.fromJson(
                                response.errorBody().string()).getReason());
                    }
                } catch (IOException e) {
                    display(e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<AckRecordDTO>> call, Throwable t) {
                display("Produce failed! Reason: " + t.getMessage());
            }
        });
    }

    public void getSchema(View view) {
        display("Get Schema clicked.");
        Call<SchemaDTO> call = hopsworksClient.getTopicSchema(
                getProjectName(), getJwtToken(), getProjectTopic());
        call.enqueue(new Callback<SchemaDTO>() {
            @Override
            public void onResponse(
                    Call<SchemaDTO> call, Response<SchemaDTO> response) {
                try {
                    if (response.isSuccessful()){
                        display("Get Schema was successful!");
                        display(response.body().getContents());
                        if(response.body().getName().equals(CoordinatesRecord.class.getName())){
                            display("Schema name matched with production!");
                        }
                    }else{
                        display(HopsWorksResponse.fromJson(
                                response.errorBody().string()).getReason());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    display(e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<SchemaDTO> call, Throwable t) {
                display("Get Schema failed! Reason: " + t.getMessage());
            }
        });

    }


    public void produceInBackground(View view) {
        RecordStreamWorker recordStreamWorker = RecordStreamWorker.getInstance(CoordinatesRecord.class);
        recordStreamWorker.timeSync();
        recordStreamWorker.clean();
        recordStreamWorker.produce(new ProduceTask(), 0, 500, TimeUnit.MILLISECONDS);
        recordStreamWorker.stream(new StreamTask(this.getApplicationContext()), 0, 10, TimeUnit.SECONDS);
        notification(this.getApplicationContext());
    }

    public static void closeStreaming(Class cls){
        RecordStreamWorker recordStreamWorker = RecordStreamWorker.getInstance(cls);
        recordStreamWorker.close();
    }

    public void getAvroSchema(View view) {
        try {
            display(AvroTemplate.getSchema(new CoordinatesRecord(1.23, 2.42)));
        } catch (SQLiteNotInitialized SQLiteNotInitialized) {
            SQLiteNotInitialized.printStackTrace();
        }

    }


    public static void notification(Context context){

        Intent intentAction = new Intent(context, CoordinatesRecordStreamReceiver.class);
        intentAction.putExtra("action","stop");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action notifyAction = new Notification.Action(
                R.drawable.ic_close, null ,  pendingIntent);

        Notification noti = new Notification.Builder(context)
                .setContentTitle("Producing in background...")
                .setContentText("Click to terminate producing thread.")
                .setSmallIcon(R.drawable.ic_room_black_24dp)
                .addAction(notifyAction)
                .build();

        NotificationManager n =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        n.notify(1, noti);
    }


    private class DisplayTask implements Runnable{

        private String message;

        DisplayTask(String message){
            this.message = message;
        }
        public void run() {
            display(message);
        }
    }


    private class ProduceTask implements Runnable{

        @Override
        public void run() {
            try {
                Random randomGenerator = new Random();
                CoordinatesRecord record = new CoordinatesRecord(
                        randomGenerator.nextDouble()*180, randomGenerator.nextDouble()*180);
                record.save();
                getDisplay().post(new DisplayTask(record.getRecordUUID() + " pro"));
            } catch (SQLiteNotInitialized SQLiteNotInitialized) {
                display(SQLiteNotInitialized.getMessage());
            }
        }
    }

    private class StreamTask implements Runnable{

        private Context context;

        public StreamTask(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            try {
                final ArrayList<Record> records =
                        RecordsTable.readAllNotAcked(CoordinatesRecord.class);

                if (records.isEmpty()){
                    getDisplay().post(new DisplayTask("No records to send."));
                    return;
                }else if(!NetworkUtils.isNetworkAvailable(context)){
                    getDisplay().post(new DisplayTask("No internet connection."));
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
                                    getDisplay().post(new DisplayTask("Length Miss match."));
                                } else {
                                    for (int i = 0; i < records.size(); i++) {
                                        Record record = records.get(i);
                                        if (acks.get(i).isAck()){
                                            record.setAcked(true);
                                            record.save();
                                            getDisplay().post(
                                                    new DisplayTask(
                                                            record.getRecordUUID() + " ack"));
                                        }

                                    }

                                }
                            } else {
                                getDisplay().post(
                                        new DisplayTask(HopsWorksResponse.fromJson(
                                                response.errorBody().string()).getReason()));
                            }
                        } catch (IOException e) {
                            display(e.getMessage());
                        } catch (SQLiteNotInitialized SQLiteNotInitialized) {
                            display(SQLiteNotInitialized.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AckRecordDTO>> call, Throwable t) {
                        display("Produce failed! Reason: " + t.getMessage());
                    }
                });

            } catch (SQLiteNotInitialized SQLiteNotInitialized) {
                SQLiteNotInitialized.printStackTrace();
            }
        }
    }

}
