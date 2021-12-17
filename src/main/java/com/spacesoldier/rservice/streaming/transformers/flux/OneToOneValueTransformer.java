package com.spacesoldier.rservice.streaming.transformers.flux;

import java.util.Map;
import java.util.function.Function;

public class OneToOneValueTransformer implements Function {
    private String name;
    private Map<Class, Function> inputValueProcessors;

    public OneToOneValueTransformer(
            String name,
            Map<Class, Function> inputValueProcessors
    ){
        this.name = name;
        this.inputValueProcessors = inputValueProcessors;
    }

    @Override
    public Object apply(Object input) {

        return
                inputValueProcessors.containsKey(input.getClass()) ?
                    inputValueProcessors.get(
                                                input.getClass()
                                            )
                                        .apply(input)               :
                    input;

    }
}
