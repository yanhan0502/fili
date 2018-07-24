// Copyright 2018 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.luthier.webservice.data.config.dimension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import com.yahoo.bard.webservice.data.config.dimension.DimensionConfig;
import com.yahoo.bard.webservice.data.dimension.KeyValueStore;
import com.yahoo.bard.webservice.data.dimension.MapStoreManager;
import com.yahoo.bard.webservice.data.dimension.SearchProvider;
import com.yahoo.bard.webservice.data.dimension.impl.ScanSearchProviderManager;
import com.yahoo.bard.webservice.util.StreamUtils;
import com.yahoo.luthier.webservice.data.config.ExternalConfigLoader;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Hold all the dimension configurations for the sample Bard instance.
 */
public class ExternalDimensionsLoader {

    private final Set<DimensionConfig> dimensionConfigs;

    /**
     * Construct the dimension configurations.
     *
     * @param dimensionConfigLoader The external configuration loader for loading dimensions
     * @param dimensionConfigFilePath The external file's url containing the external config information
     */
    public ExternalDimensionsLoader(
            ExternalConfigLoader dimensionConfigLoader,
            String dimensionConfigFilePath
    ) {

        JodaModule jodaModule = bindTemplates();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(jodaModule);

        ExternalDimensionConfigTemplate dimensionConfig =
                dimensionConfigLoader.parseExternalFile(
                        dimensionConfigFilePath + "DimensionConfig.json",
                        ExternalDimensionConfigTemplate.class, objectMapper);

        this.dimensionConfigs = Collections.unmodifiableSet(
                dimensionConfig.getDimensions().stream()
                        .map(dimension -> dimension.build(dimensionConfig.getFieldSets()))
                        .collect(Collectors.toSet())
        );

        dimensionConfigs.stream().collect(
                StreamUtils.toLinkedMap(DimensionConfig::getApiName, Function.identity())
        );

    }

    /**
     * Get all dimension configurations.
     *
     * @return set of dimension configurations
     */
    public Set<DimensionConfig> getAllDimensionConfigurations() {
        return dimensionConfigs;
    }

    /**
     * Get dimension configurations dictionary.
     *
     * @return a map from dimension name to dimension configurations
     */
    public Map<String, DimensionConfig> getDimensionConfigurations() {
        Map<String, DimensionConfig> dimensionConfigMap = new HashMap<>();
        for (DimensionConfig dimensionConfig : dimensionConfigs) {
            dimensionConfigMap.put(dimensionConfig.getApiName(), dimensionConfig);
        }
        return dimensionConfigMap;
    }

    /**
     * Templates and deserializers binder.
     *
     * @return A Joda Module contains binding information.
     */
    private JodaModule bindTemplates() {
        JodaModule jodaModule = new JodaModule();
        jodaModule.addAbstractTypeMapping(ExternalDimensionConfigTemplate.class,
                DefaultExternalDimensionConfigTemplate.class);
        jodaModule.addAbstractTypeMapping(DimensionFieldInfoTemplate.class,
                DefaultDimensionFieldInfoTemplate.class);
        jodaModule.addAbstractTypeMapping(DimensionTemplate.class,
                DefaultDimensionTemplate.class);
        jodaModule.addAbstractTypeMapping(DimensionFieldListTemplate.class,
                DefaultDimensionFieldListTemplate.class);
        jodaModule.addDeserializer(DefaultDimensionFieldListTemplate.class,
                DefaultDimensionFieldInfoTemplate.deserializer());
        return jodaModule;
    }
}
