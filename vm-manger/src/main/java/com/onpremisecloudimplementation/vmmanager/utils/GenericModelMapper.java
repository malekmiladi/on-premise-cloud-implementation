package com.onpremisecloudimplementation.vmmanager.utils;
import java.lang.reflect.Type;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GenericModelMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public <S, D> D map(S source, Type destinationType) {
        return modelMapper.map(source, destinationType);
    }

}
