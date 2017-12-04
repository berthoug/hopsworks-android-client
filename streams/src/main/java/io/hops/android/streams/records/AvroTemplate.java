package io.hops.android.streams.records;

//Uses the Avro dependency
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;

public class AvroTemplate{

    public static String getSchema(Class cls){
        if (cls != null){
            Schema schema = ReflectData.get().getSchema(cls.getClass());
            if (schema != null){
                return schema.toString();
            }
        }
        return null;
    }

    public static String getSchema(Object obj){
        if (obj != null){
            Schema schema = ReflectData.get().getSchema(obj.getClass());
            if (schema != null){
                return schema.toString();
            }
        }
        return null;
    }

}
