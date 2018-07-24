// Copyright 2018 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.luthier.webservice.data.config.dimension;

import java.util.List;

/**
 * Contains field name and a list of field template.
 * For DimensionFieldDeserializer
 * <p>
 *        field name: "xxx"
 *        field list: [field A, field B, field C]
 */
public class DefaultDimensionFieldListTemplate implements DimensionFieldListTemplate {

    private String fieldName;
    private List<DimensionFieldInfoTemplate> fieldList;

    @Override
    public void setFieldName(String name) {
        this.fieldName = name;
    }

    @Override
    public void setFieldList(List<DimensionFieldInfoTemplate> list) {
        sortFields(list);
        this.fieldList = list;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public List<DimensionFieldInfoTemplate> getFieldList() {
        return fieldList;
    }
}
