// Copyright 2018 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.luthier.webservice.data.config.dimension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

import com.yahoo.bard.webservice.data.config.dimension.DefaultKeyValueStoreDimensionConfig;
import com.yahoo.bard.webservice.data.config.dimension.DimensionConfig;
import com.yahoo.bard.webservice.data.config.names.DimensionName;
import com.yahoo.bard.webservice.data.dimension.*;
import com.yahoo.bard.webservice.data.dimension.impl.ScanSearchProviderManager;
import com.yahoo.bard.webservice.util.EnumUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dimension template.
 * <p>
 * An example:
 * <p>
 *      {
 *          "apiName": "REGION_ISO_CODE",
 *          "longName": "wiki regionIsoCode",
 *          "description": "Iso Code of the region to which the wiki page belongs",
 *          "fields": "default"
 *      }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultDimensionTemplate implements DimensionTemplate {

    private final String apiName;
    private final String description;
    private final String longName;
    private final String category;
    private DimensionFieldListTemplate fields;

    /**
     * Constructor used by json parser.
     *
     * @param apiName json property apiName
     * @param description json property description
     * @param longName json property longName
     * @param category json property category
     * @param fields json property fields deserialize by DimensionFieldDeserializer
     */
    @JsonCreator
    public DefaultDimensionTemplate(
            @NotNull @JsonProperty("apiName") String apiName,
            @JsonProperty("description") String description,
            @JsonProperty("longName") String longName,
            @JsonProperty("category") String category,
            @JsonProperty("fields") DimensionFieldListTemplate fields
    ) {
        this.apiName = apiName;
        this.description = (Objects.isNull(description) ? "" : description);
        this.longName = (Objects.isNull(longName) ? apiName : longName);
        this.category = (Objects.isNull(category) ? Dimension.DEFAULT_CATEGORY : category);
        this.fields = fields;
    }

    /**
     * Get dimensions info.
     */
    @Override
    public String getApiName() {
        return this.apiName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getLongName() {
        return this.longName;
    }

    @Override
    public String getCategory() {
        return this.category;
    }

    @Override
    public String toString() {
        return this.getApiName();
    }

    @Override
    public LinkedHashSet<DimensionField> getFields(
            Map<String, List<DimensionFieldInfoTemplate>> fieldDictionary
    ) {
        resolveFields(fieldDictionary);
        if (this.fields.getFieldList() == null) {
            return new LinkedHashSet<>();
        }
        return this.fields.getFieldList().stream()
                .map(DimensionFieldInfoTemplate::build)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public DimensionConfig build(Map<String, List<DimensionFieldInfoTemplate>> fieldSet) {
        return new DefaultKeyValueStoreDimensionConfig(
                () -> (getApiName()),
                getApiName(),
                getDescription(),
                getLongName(),
                getCategory(),
                getFields(fieldSet),
                getDefaultKeyValueStore(),
                getDefaultSearchProvider()
        );
    }

    /**
     * Lazily provide a KeyValueStore for this store name.
     *
     * @return A KeyValueStore instance
     */
    private KeyValueStore getDefaultKeyValueStore() {
        return MapStoreManager.getInstance(getApiName());
    }

    /**
     * Lazily create a Scanning Search Provider for this provider name.
     *
     * @return A Scanning Search Provider for the provider name.
     */
    private SearchProvider getDefaultSearchProvider() {
        return ScanSearchProviderManager.getInstance(getApiName());
    }

    /**
     * Parse fields info based on dimension's "field" key word.
     * <p>
     * If "field list is no empty", use fields in field list
     * If "no field list" and "field has a name", map name in fieldSetInfo to get a field list
     * If "no field list" and "no field name", use default field list in fieldSetInfo
     *
     * @param fieldDictionary a map from fieldset's name to fieldset
     */
    private void resolveFields(
            Map<String, List<DimensionFieldInfoTemplate>> fieldDictionary
    ) {

        if (fieldDictionary == null) {
            return;
        }

        // if specific fields
        if (this.fields != null && this.fields.getFieldList() != null) {
            this.fields.setFieldName("Specific");
        }

        // default fields
        else if (this.fields == null || this.fields.getFieldName() == null && this.fields.getFieldList() == null) {
            this.fields = new DefaultDimensionFieldListTemplate();
            this.fields.setFieldName("Default");
            this.fields.setFieldList(fieldDictionary.get("default"));
        }

        // named fields
        else if (fieldDictionary.containsKey(this.fields.getFieldName())) {
            this.fields.setFieldList(fieldDictionary.get(this.fields.getFieldName()));
        }

        // others -> default
        else {
            this.fields = new DefaultDimensionFieldListTemplate();
            this.fields.setFieldName("Default");
            this.fields.setFieldList(fieldDictionary.get("default"));
        }
    }
}
