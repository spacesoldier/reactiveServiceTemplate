package com.spacesoldier.rservice.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class GsonSerializer<T> implements Serializer<T> {

//    private Gson gson = new Gson();
//
//    @Override
//    public void close() {
//    }
//
//    @Override
//    public void configure(Map<String, ?> arg0, boolean arg1) {
//    }
//
//    @Override
//    public byte[] serialize(String topic, T value) {
//        return gson.toJson(value).getBytes();
//        // when need to strictly use UTF-8 encoding:
////        return gson.toJson(value).getBytes(Charset.forName("UTF-8"));
//    }

    private Gson gson = new GsonBuilder().create();

    @Override
    public void configure(Map<String, ?> config, boolean isKey) {
        // this is called right after construction
        // use it for initialisation
    }

    @Override
    public byte[] serialize(String s, T t) {
        return gson.toJson(t).getBytes();
    }

    @Override
    public void close() {
        // this is called right before destruction
    }

}
