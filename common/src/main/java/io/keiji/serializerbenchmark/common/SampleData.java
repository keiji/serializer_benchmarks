package io.keiji.serializerbenchmark.common;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

@Message
public class SampleData {

    @Message
    public enum Gender {
        Female,
        Male;
    }

    // https://github.com/msgpack/msgpack-java/issues/98
    @Index(0)
    private long id;

    @Index(1)
    private String name;

    @Index(2)
    private int age;

    @Index(3)
    private Gender gender;

    @Index(4)
    private boolean isMegane;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public boolean isMegane() {
        return isMegane;
    }

    public void setMegane(boolean megane) {
        isMegane = megane;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleData that = (SampleData) o;

        if (id != that.id) return false;
        if (age != that.age) return false;
        if (isMegane != that.isMegane) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return gender == that.gender;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (isMegane ? 1 : 0);
        return result;
    }

    public static class Template extends AbstractTemplate<SampleData> {

        private Template() {
        }

        public static Template getInstance() {
            return new Template();
        }


        public void write(Packer pk, SampleData v, boolean required) throws IOException {
            pk.writeArrayBegin(5)
                    .write(v.id)
                    .write(v.name)
                    .write(v.age)
                    .write(v.gender.ordinal())
                    .write(v.isMegane)
                    .writeArrayEnd();
        }

        public SampleData read(Unpacker u, SampleData to, boolean required) throws IOException {
            if (to == null) {
                to = new SampleData();
            }

            u.readArrayBegin();
            to.id = u.readLong();
            to.name = u.readString();
            to.age = u.readInt();
            to.gender = Gender.values()[u.readInt()];
            to.isMegane = u.readBoolean();
            u.readArrayEnd();

            return to;
        }
    }
}
