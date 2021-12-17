package com.spacesoldier.rservice.streaming.transformers.kafka;

import org.apache.kafka.streams.kstream.ValueMapperWithKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LogicUnitValueMapper implements ValueMapperWithKey<Object, Object, List<Object>> {
    private String name;
    private Map<Class, BiFunction> inputValueProcessors;
    private Map<Class, BiFunction> outputValueProcessors;
    private BiFunction errorHandlerProcessor;

    public LogicUnitValueMapper(Map<Class, BiFunction> inputValueProcessors,
                                Map<Class,BiFunction> outputValueProcessors,
                                BiFunction errorHandlerProcessor,
                                String name){
        this.inputValueProcessors = inputValueProcessors;
        this.outputValueProcessors = outputValueProcessors;
        this.errorHandlerProcessor = errorHandlerProcessor;
        this.name = name;
    }

    @Override
    public List<Object> apply(Object key, Object value) {

        List<Object> results = new ArrayList<>();

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
        // by Spring Cloud Stream Kafka Streams binder instead of MessageRouter
        // which is less effective in terms of speed and code complexity
        //
        // note that in KStream.flatMapValues transformations the key does not change

        Object currentValue = inputValueProcessors.containsKey(value.getClass()) ?
                inputValueProcessors.get(value.getClass()).apply(key, value) :
                value;

        if (currentValue!= null){

            if (currentValue instanceof List){
                // we allow the input value stream to produce additional streams
                // for example some logs or aggregations along with the value transformations
                List<Object> valuesOut = (List<Object>) currentValue;
                results.addAll(
                        valuesOut
                                .stream()   // could use flatMap to let the transformations return multiple items
                                .map(
                                        outValue ->
                                                outputValueProcessors != null &&
                                                        outputValueProcessors.containsKey(outValue.getClass()) ?
                                                        outputValueProcessors.get(outValue.getClass())
                                                                .apply(key, outValue)          :
                                                        outValue
                                ).collect(Collectors.toList())
                );
            } else {
                results.add(    outputValueProcessors != null &&
                        outputValueProcessors.containsKey(currentValue.getClass()) ?
                        outputValueProcessors.get(currentValue.getClass())
                                .apply(key, currentValue)      :
                        currentValue
                );
            }

        } else {
            if (errorHandlerProcessor != null){
                results.add(errorHandlerProcessor.apply(key,value));
            }
        }

        return results;
    }
}
