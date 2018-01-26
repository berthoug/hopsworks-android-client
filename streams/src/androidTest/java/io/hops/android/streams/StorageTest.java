package io.hops.android.streams;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.hops.android.streams.records.CoordinatesRecord;
import io.hops.android.streams.storage.PropertiesTable;
import io.hops.android.streams.storage.RecordsTable;
import io.hops.android.streams.storage.SQLite;
import io.hops.android.streams.storage.TimestampsTable;
import io.hops.android.streams.time.Timer;
import io.hops.android.streams.time.Timestamp;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class StorageTest {
    @Test
    public void testAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("io.hops.android.streams.test", appContext.getPackageName());
    }

    @Test
    public void testPropertiesPersistence() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLite.init(appContext);

        String property = "testProperty";
        String value = "2";
        String updated_value = "5";

        PropertiesTable.delete(property);
        assertEquals(PropertiesTable.read(property), null);
        assertEquals(PropertiesTable.write(property, value), true);
        assertEquals(PropertiesTable.read(property), value);
        assertEquals(PropertiesTable.write(property, updated_value), true);
        assertEquals(PropertiesTable.read(property), updated_value);
        PropertiesTable.delete(property);
    }

    @Test
    public void testRecordsPersistence() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLite.init(appContext);

        CoordinatesRecord record = new CoordinatesRecord(59.3315, 18.0715);
        assertEquals(record.save(), true);

        CoordinatesRecord retrievedRecord = (CoordinatesRecord) RecordsTable.read(
                record.getRecordUUID(), CoordinatesRecord.class);
        assertEquals(record.getRecordUUID(), retrievedRecord.getRecordUUID());
        assertEquals(record.getLatitude(), retrievedRecord.getLatitude(), 0.001);
        assertEquals(record.getLongitude(), retrievedRecord.getLongitude(), 0.001);
        record.delete();
        retrievedRecord = (CoordinatesRecord) RecordsTable.read(
                record.getRecordUUID(), CoordinatesRecord.class);
        assertEquals(retrievedRecord, null);
    }

    @Test
    public void testMaxTimestamp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLite.init(appContext);

        Timestamp refTimestamp = Timer.getInstance().getClonedReferenceTimestamp();
        Timestamp storedTimestamp = TimestampsTable.loadMaxBootNumTimestamp();
        assertEquals(refTimestamp.getBootNum(), storedTimestamp.getBootNum());
        assertEquals(refTimestamp.getBootMillis(), storedTimestamp.getBootMillis());
    }

    @Test
    public void testTimestampMemoryStorageSync() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLite.init(appContext);

        Timestamp refTimestamp = Timer.getInstance().getClonedReferenceTimestamp();
        Timestamp storedTimestamp = TimestampsTable.loadMaxBootNumTimestamp();
        assertEquals(refTimestamp.getBootNum(), storedTimestamp.getBootNum());
        assertEquals(refTimestamp.getBootMillis(), storedTimestamp.getBootMillis());
    }
}
