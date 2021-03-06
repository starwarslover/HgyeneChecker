package com.example.serba.hygenechecker.models;

/**
 * Created by serba on 28/02/2018.
 */

public class BusinessType extends AAdvancedSearchParam {
    private String BusinessTypeId;
    private String BusinessTypeName;

    public BusinessType(String businessTypeId, String businessTypeName) {
        BusinessTypeId = businessTypeId;
        BusinessTypeName = businessTypeName;
    }

    @Override
    public String getId() {
        return BusinessTypeId;
    }

    @Override
    public String getDisplayName() {
        return BusinessTypeName;
    }
}
