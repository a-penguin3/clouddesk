package com.zybio.clouddesk.config;


import com.zybio.clouddesk.enums.Regions;

import java.util.Map;

public final class EncryptConfig {

    public static final Map<Regions,Integer> REGION = Map.of(Regions.REGION1,0,Regions.REGION2,40);
}
