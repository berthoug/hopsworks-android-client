
# hopsworks-android-client

### Purpose
To provide a means to stream records in a secure and reliable way into hopsworks.

### Structure
This project contains 2 Modules. The *streams* module is the core library, while the *center* module is a prototype application that demonstrates the usage of the library.

### Requirements 
* Minimum Android SDK API level 19 (Android version 4.4)

### Usage

It is important to understand that the library requires an SQLite database and in order to be used the SQLite must be initialized and a connection to it must be established before any call is made. You can do so by the following code:
```java
SQLite.init(this.getApplicationContext());
```
Should a call to the library is made without initializing the SQLite database an SQLiteNotInitialized exception could be thrown.

To define the type of Record that you want to produce and stream, you can do so by extending the io.hops.android.streams.storage.records.Record class. For example, if we wanted to have a CoordinatesRecord here is how this can be achieved:

```java
import io.hops.android.streams.storage.SQLiteNotInitialized;

public class CoordinatesRecord extends Record{

    private double latitude;

    private double longitude;

    public CoordinatesRecord(double latitude, double longitude) throws SQLiteNotInitialized {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

}
```

 When the record is generated it automatically takes a relative timestamp since reboot.
 In order to save the record in the SQLite database you should call the save method of the record.
 
 ```java
 record.save()
 ```
