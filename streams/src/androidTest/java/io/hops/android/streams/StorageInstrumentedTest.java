package io.hops.android.streams;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.hops.android.streams.records.AvroTemplate;
import io.hops.android.streams.records.CoordinatesRecord;
import io.hops.android.streams.storage.Storage;
import io.hops.android.streams.time.Timer;
import io.hops.android.streams.time.Timestamp;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class StorageInstrumentedTest {
    @Test
    public void testAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("io.hops.android.streams.test", appContext.getPackageName());
    }

    @Test
    public void testPropertiesPersistence() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Storage storage = Storage.init(appContext);

        String property = "testProperty";
        String value = "2";
        String updated_value = "5";

        storage.deleteProperty(property);
        assertEquals(storage.loadProperty(property), null);
        assertEquals(storage.saveProperty(property, value), true);
        assertEquals(storage.loadProperty(property), value);
        assertEquals(storage.saveProperty(property, updated_value), true);
        assertEquals(storage.loadProperty(property), updated_value);
        storage.deleteProperty(property);
    }

    @Test
    public void testRecordsPersistence() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Storage storage = Storage.init(appContext);

        CoordinatesRecord record = new CoordinatesRecord(59.3315, 18.0715);
        assertEquals(storage.saveRecord(record), true);

        CoordinatesRecord retrievedRecord = (CoordinatesRecord) storage.loadRecord(
                record.getRecordUUID(), CoordinatesRecord.class);
        assertEquals(record.getRecordUUID(), retrievedRecord.getRecordUUID());
        assertEquals(record.getLatitude(), retrievedRecord.getLatitude(), 0.001);
        assertEquals(record.getLongitude(), retrievedRecord.getLongitude(), 0.001);
        storage.deleteRecord(record);
        retrievedRecord = (CoordinatesRecord) storage.loadRecord(
                record.getRecordUUID(), CoordinatesRecord.class);
        assertEquals(retrievedRecord, null);
    }

    @Test
    public void testMaxTimestamp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Storage storage = Storage.init(appContext);
        Timestamp refTimestamp = Timer.getInstance().getClonedReferenceTimestamp();
        Timestamp storedTimestamp = storage.loadMaxBootNumTimestamp();
        assertEquals(refTimestamp.getBootNum(), storedTimestamp.getBootNum());
        assertEquals(refTimestamp.getBootMillis(), storedTimestamp.getBootMillis());
    }

    @Test
    public void testTimestampMemoryStorageSync() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Storage storage = Storage.init(appContext);
        Timestamp refTimestamp = Timer.getInstance().getClonedReferenceTimestamp();
        Timestamp storedTimestamp = storage.loadMaxBootNumTimestamp();
        assertEquals(refTimestamp.getBootNum(), storedTimestamp.getBootNum());
        assertEquals(refTimestamp.getBootMillis(), storedTimestamp.getBootMillis());
    }
}
