package io.hops.android.streams;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.hops.android.streams.storage.SQLite;
import io.hops.android.streams.time.Timer;
import io.hops.android.streams.time.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TimerInstrumentedTest {

    @Test
    public void testTimeSync() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLite.init(appContext);
        Timestamp refTimestamp = Timer.getInstance().getClonedReferenceTimestamp();
        Timer.getInstance().sync("pool.ntp.org", 5000);
        Timestamp refTimestampNew = Timer.getInstance().getClonedReferenceTimestamp();
        Assert.assertTrue(refTimestampNew.getBootMillis() > refTimestamp.getBootMillis());
        Assert.assertTrue(refTimestampNew.getBootMillis() > 0);
        Assert.assertTrue(Timer.getInstance().getTimestamp().getEpochMillis() > 0);
    }

    /**
     * Tests that successive timestamp calls are not more than a millisecond appart.
     */
    @Test
    public void testTimestampQuality() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLite.init(appContext);
        Timer.getInstance().sync("pool.ntp.org", 5000);
        for(int i=0; i<1000; i++){
            Timestamp timestampPrev = Timer.getInstance().getTimestamp();
            Timestamp timestampCurr = Timer.getInstance().getTimestamp();
            Assert.assertTrue(timestampCurr.getBootMillis()-timestampPrev.getBootMillis()<=1);
            Assert.assertTrue(timestampCurr.getEpochMillis()-timestampPrev.getEpochMillis()<=1);
        }

    }




}
