package io.keiji.serializerbenchmark;

import android.os.Build;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.keiji.serializerbenchmark.common.SampleData;
import io.keiji.serializerbenchmark.common.SampleData.Gender;

public class MsgPackTest {
    private static final String TAG = MsgPackTest.class.getSimpleName();

    private static final int LIMIT = 30;
    private static final int EPOCH = 5;

    private final Random rand = new Random();
    private final List<SampleData> userList = new ArrayList<>();

    @Before
    public void prepare() throws Exception {

        for (int i = 0; i < LIMIT; i++) {
            SampleData data1 = generateSample(i);
            userList.add(data1);
        }
    }

    @NonNull
    private SampleData generateSample(long id) {
        SampleData data1 = new SampleData();
        data1.setId(id);
        data1.setName("user " + id);
        data1.setAge(rand.nextInt(50));
        data1.setGender(rand.nextBoolean() ? Gender.Female : Gender.Male);
        data1.setMegane(rand.nextBoolean());
        return data1;
    }

    private final Template<List<SampleData>> listTmpl
            = Templates.tList(SampleData.Template.getInstance());

    @Test
    public void test() throws Exception {

        for (int i = 0; i < EPOCH; i++) {
            onshotTest();
        }
    }

    private void onshotTest() throws Exception {
        Result result = serializeDeserialize();
        Log.d(TAG, result.toString());

        for (int i = 0; i < userList.size(); i++) {
            Assert.assertTrue(userList.get(i).equals(result.serializedList.get(i)));
        }
    }

    private Result serializeDeserialize() throws Exception {
        MessagePack msgPack = new MessagePack();

        // serialize
        long start = Debug.threadCpuTimeNanos();
        byte[] serializedData = msgPack.write(userList, listTmpl);
        long serializeDuration = Debug.threadCpuTimeNanos() - start;

        long serializedSize = serializedData.length;

        // deserialize
        start = Debug.threadCpuTimeNanos();
        List<SampleData> list = msgPack.read(serializedData, listTmpl);
        long deserializeDuration = Debug.threadCpuTimeNanos() - start;

        return new Result(list, serializeDuration, serializedSize, deserializeDuration);
    }

    private class Result {
        public final List<SampleData> serializedList;
        public final long serializeDuration;
        public final long serializedSize;
        public final long deserializeDuration;


        private Result(List<SampleData> serializedList,
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
