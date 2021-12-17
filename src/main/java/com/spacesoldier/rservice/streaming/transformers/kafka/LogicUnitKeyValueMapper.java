package com.spacesoldier.rservice.streaming.transformers.kafka;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LogicUnitKeyValueMapper implements KeyValueMapper<Object, Object, List<KeyValue<Object, Object>>> {

    private String name;
    private Map<Class, BiFunction<Object, Object, List<KeyValue<Object,Object>>>> inputValueProcessors;
    private Map<Class, BiFunction<Object, Object, List<KeyValue<Object,Object>>>> outputValueProcessors;
    private BiFunction<Object, Object, KeyValue<Object,Object>> errorHandlerProcessor;

    public LogicUnitKeyValueMapper(Map<Class, BiFunction<Object, Object, List<KeyValue<Object,Object>>>> inputValueProcessors,
                                   Map<Class,BiFunction<Object, Object, List<KeyValue<Object,Object>>>> outputValueProcessors,
                                   BiFunction errorHandlerProcessor,
                                   String name)
    {
        this.inputValueProcessors = inputValueProcessors;
        this.outputValueProcessors = outputValueProcessors;
        this.errorHandlerProcessor = errorHandlerProcessor;
        this.name = name;
    }

    @Override
    public List<KeyValue<Object, Object>> apply(Object key, Object value) {
        List<KeyValue<Object, Object>> results = new ArrayList<>();

        // Apply mapping logic to input values which have the certain type.
        // Values of other types will pass without changes.
        // The purpose of this logic is to be applied in the long chain
        // of KStream.flatMapValues calls, every one of which generates
        // multiple values of various types, such as:
        // --- data which will be used in next nodes of this chain
        // --- log messages
        // --- additional pieces of data inserted into the chain from outer scope
        // So the data which is going through the chain is processed only when needed,
        // generates additional data flows which handled in same logic chain
        // which helps to use the existing data bindings provided
        // by Spring Cloud Stream Kafka Streams binder instead of StreamBridge wrapper
        // which is much less effective in terms of speed and code complexity
        //
        // note that in KStream.flatMap transformations the key do not have to be the same for different messages

        List<KeyValue<Object,Object>> valuesOut = inputValueProcessors.containsKey(value.getClass())     ?
                                            inputValueProcessors.get(value.getClass()).apply(key, value)    :
                                            new ArrayList<>()   {{ add(KeyValue.pair(key, value)); }};


        if (valuesOut!= null){

            // we allow the input value stream to produce additional streams
            // for example some logs or aggregations along with the value transformations

           List outputs =
                   valuesOut
                    .stream()   // could use flatMap to let the transformations return multiple items
                    .map(
                            outValueObj ->{
                                KeyValue<Object,Object> outValue = (KeyValue<Object,Object>) outValueObj;
                                return outputValueProcessors != null &&
                                        outputValueProcessors.containsKey(outValue.value.getClass()) ?
                                            outputValueProcessors.get(outValue.getClass())
                                                        .apply(outValue.key, outValue.value)         :
                                        outValue;
                            }

                    ).collect(Collectors.toList());

            results.addAll(
                        outputs
            );


        } else {
            if (errorHandlerProcessor != null){
                results.add(errorHandlerProcessor.apply(key,value));
            }
        }


        return results;
    }
}
