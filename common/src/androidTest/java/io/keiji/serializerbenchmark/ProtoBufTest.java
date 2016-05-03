package io.keiji.serializerbenchmark;

import android.os.Build;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.Random;

import io.keiji.serializerbenchmark.common.Sampledata;
import io.keiji.serializerbenchmark.common.Sampledata.SampleData;

// https://developers.google.com/protocol-buffers/docs/javatutorial
public class ProtoBufTest {
    private static final String TAG = ProtoBufTest.class.getSimpleName();

    private static final int LIMIT = 30;
    private static final int EPOCH = 5;

    private final Random rand = new Random();
    private Sampledata.SampleList userList = Sampledata.SampleList.newBuilder().build();

    @Before
    public void prepare() throws Exception {

        Sampledata.SampleList.Builder builder = userList.toBuilder();

        for (int i = 0; i < LIMIT; i++) {
            SampleData data1 = generateSample(i);
            builder.addSampleData(data1);
        }

        userList = builder.build();
    }

    @NonNull
    private SampleData generateSample(long id) {
        SampleData.Builder builder = SampleData
                .newBuilder()
                .setId(id)
                .setName("user " + id)
                .setAge(rand.nextInt(50))
                .setGender(rand.nextBoolean() ? SampleData.Gender.Female : SampleData.Gender.Male)
                .setIsMegane(rand.nextBoolean() ? 1 : 0);
        return builder.build();
    }

    @Test
    public void test() throws Exception {

        for (int i = 0; i < EPOCH; i++) {
            onshotTest();
        }
    }

    private void onshotTest() throws Exception {
        Result result = serializeDeserialize();
        Log.d(TAG, result.toString());

        for (int i = 0; i < userList.getSampleDataCount(); i++) {
            Assert.assertTrue(userList.getSampleData(i).equals(result.serializedList.getSampleData(i)));
        }
    }

    private Result serializeDeserialize() throws Exception {

        // serialize
        long start = Debug.threadCpuTimeNanos();
        byte[] serializedData = userList.toByteArray();
        long serializeDuration = Debug.threadCpuTimeNanos() - start;

        long serializedSize = serializedData.length;

        // deserialize
        start = Debug.threadCpuTimeNanos();
        Sampledata.SampleList list = Sampledata.SampleList.parseFrom(serializedData);
        long deserializeDuration = Debug.threadCpuTimeNanos() - start;

        return new Result(list, serializeDuration, serializedSize, deserializeDuration);
    }

    private class Result {
        public final Sampledata.SampleList serializedList;
        public final long serializeDuration;
        public final long serializedSize;
        public final long deserializeDuration;


        private Result(Sampledata.SampleList serializedList,
                       long serializeDuration,
                       long serializedSize,
                       long deserializeDuration) {
            this.serializedList = serializedList;
            this.serializeDuration = serializeDuration;
            this.serializedSize = serializedSize;
            this.deserializeDuration = deserializeDuration;
        }

        @Override
        public String toString() {
            return String.format(Locale.getDefault(),
                    "Device: " + Build.DEVICE + "\n" +
                            "serialize:   duration -> %dns\n" +
                            "             size -> %dbytes\n" +
                            "deserialize: duration -> %dns",
                    serializeDuration, serializedSize, deserializeDuration);
        }
    }
}
