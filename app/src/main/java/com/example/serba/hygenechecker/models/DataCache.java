package com.example.serba.hygenechecker.models;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by serba on 28/02/2018.
 */

public class DataCache {
    private static DataCache instance;
    private ArrayList<AAdvancedSearchParam> businessTypes;
    private ArrayList<AAdvancedSearchParam> regions;
    private ArrayList<AAdvancedSearchParam> authorities;
    private ArrayList<String> favouritesIds;

    private DataCache() {
        businessTypes = new ArrayList<>();
        regions = new ArrayList<>();
        authorities = new ArrayList<>();
        favouritesIds = new ArrayList<>();

        businessTypes.add(new BusinessType(null, "All"));
        regions.add(new Region(null, "All"));
    }

    public static synchronized DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }


    public ArrayList<AAdvancedSearchParam> getBusinessTypes() {
        return businessTypes;
    }

    public ArrayList<AAdvancedSearchParam> getRegions() {
        return regions;
    }

    public ArrayList<AAdvancedSearchParam> getAuthorities() {
        return authorities;
    }

    public void addAuthority(LocalAuthority authority) {
        if (authority != null) {
            this.authorities.add(authority);
        }
    }

    public void addRegion(Region region) {
        if (region != null) {
            this.regions.add(region);
        }
    }

    public void addBusinessType(BusinessType businessType) {
        if (businessType != null && !businessType.getId().equals("-1")) {
            this.businessTypes.add(businessType);
        }
    }

    public ArrayList<AAdvancedSearchParam> getAuthoritiesForRegion(String regionName) {
        ArrayList<AAdvancedSearchParam> results = new ArrayList<>();
        for (AAdvancedSearchParam authority : this.authorities) {
            if (regionName.equals(((LocalAuthority) authority).getRegionName())) {
                results.add(authority);
            }
        }
        return results;
    }

    public ArrayList<String> getFavouritesIds() {
        return favouritesIds;
    }

    public void setFavouritesIds(String[] favouritesIds) {
        this.favouritesIds.addAll(Arrays.asList(favouritesIds));
    }

    public void addFavouriteId(String fhrsid) {
        this.favouritesIds.add(fhrsid);
    }

    public void removeFavouriteId(String fshid) {
        this.favouritesIds.remove(fshid);
    }
}
