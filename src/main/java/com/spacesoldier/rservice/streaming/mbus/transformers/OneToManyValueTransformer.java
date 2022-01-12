package com.spacesoldier.rservice.streaming.mbus.transformers;

import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OneToManyValueTransformer implements Function<Object, Flux> {
    private String name;
    private Map<Class, Function> inputValueProcessors;

    public OneToManyValueTransformer(
            String name,
            Map<Class, Function> inputValueProcessors
    ){
        this.name = name;
        this.inputValueProcessors = inputValueProcessors;
    }

    @Override
    public Flux apply(Object input) {
        List<Object> results = new LinkedList<>();

        Object transformed = inputValueProcessors.containsKey(input.getClass()) ?
                inputValueProcessors.get(input.getClass()).apply(input) :
                input;

        if (transformed instanceof List){
            ((List) transformed).forEach(
                    item -> results.add(item)
            );
        } else {
            results.add(transformed);
        }

        return Flux.fromIterable(results);
    }
}
