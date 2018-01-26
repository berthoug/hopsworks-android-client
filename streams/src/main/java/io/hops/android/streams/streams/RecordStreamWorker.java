package io.hops.android.streams.streams;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.hops.android.streams.records.Record;


public class RecordStreamWorker {

    private final String name;
    private final Class<? extends Record> recordType;
    private ScheduledExecutorService worker;
    private Map<String, ScheduledFuture> tasks = new HashMap<>();

    private ScheduledFuture producing;
    private ScheduledFuture streaming;
    private ScheduledFuture cleaning;
    private ScheduledFuture timeSyncing;


    private static final Object lock = new Object();
    private static Map<String, RecordStreamWorker> streams = new HashMap<>();

    public static RecordStreamWorker getInstance(Class<? extends Record> recordClass) {
        if (recordClass == null){
            return null;
        }
        String name = recordClass.getName();

        if (streams.get(name) == null){
            synchronized (lock) {
                if (streams.get(name) == null) {
                    synchronized (lock) {
                        if (streams.get(name) == null) {
                            streams.put(name, new RecordStreamWorker(recordClass));
                        }
                    }
                }
            }
        }
        return streams.get(name);
    }

    private RecordStreamWorker(Class<? extends Record> recordType){
        this.recordType = recordType;
        this.name = recordType.getName();
        this.worker = Executors.newSingleThreadScheduledExecutor();
    }

    public String getName() {
        return name;
    }

    public Class<? extends Record> getRecordType() {
        return recordType;
    }

    public boolean isProducing() {
        return (producing != null);
    }

    public boolean isStreaming() {
        return (streaming != null);
    }

    public boolean isCleaning() {
        return (cleaning != null);
    }

    public boolean isTimeSyncing() {
        return (timeSyncing != null);
    }

    public void produce(Runnable runnable, long initDelay, long delay, TimeUnit timeUnit){
        if (producing == null){
            producing = worker.scheduleAtFixedRate(runnable, initDelay, delay, timeUnit);
        }
    }

    public void stream(Runnable runnable, long initDelay, long delay, TimeUnit timeUnit){
        if (streaming == null) {
            streaming = worker.scheduleAtFixedRate(runnable, initDelay, delay, timeUnit);
        }
    }

    public void clean(Runnable runnable, long initDelay, long delay, TimeUnit timeUnit) {
        if (cleaning == null) {
            cleaning = worker.scheduleAtFixedRate(runnable, initDelay, delay, timeUnit);
        }
    }

    public void clean(){
        this.clean(new CleanTask(recordType), 0, 10, TimeUnit.MINUTES);
    }


    public void timeSync(Runnable runnable, long initDelay, long delay, TimeUnit timeUnit) {
        if (timeSyncing == null) {
            timeSyncing = worker.scheduleAtFixedRate(runnable, initDelay, delay, timeUnit);
        }
    }

    public void timeSync(){
        this.timeSync(
                new TimeSyncTask(recordType), 0, 1, TimeUnit.HOURS);
    }

    public void stopProducing(){
        if (this.isProducing()) {
            producing.cancel(false);
            producing = null;
        }
    }

    public void stopStreaming(){
        if (this.isStreaming()) {
            streaming.cancel(false);
            streaming = null;
        }
    }

    public void stopCleaning(){
        if (this.isCleaning()) {
            cleaning.cancel(false);
            cleaning = null;
        }
    }

    public void stopTimeSyncing(){
        if (this.isTimeSyncing()) {
            timeSyncing.cancel(false);
            timeSyncing = null;
        }
    }

    public void close(){
        stopProducing();
        stopStreaming();
        stopTimeSyncing();
        stopCleaning();
    }

    public static void closeStream(Class<? extends Record> cls){
        RecordStreamWorker.getInstance(cls).close();
    }

    public static void printNumOfThreadsToConsole(){
        System.out.println("Number of Active Threads: " + Thread.activeCount());
    }

}