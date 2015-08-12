package net.doodcraft.Dooder07.Stargates.Wormhole.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum StargateKeyring {
    DIAL("DialStargate"),
    SIGN("SignStargate"),
    NONE("UndefinedStargate");
    
    private static final Map<String, StargateKeyring> mapping = new HashMap<String, StargateKeyring>();
    
    static {
        for (StargateKeyring kr : EnumSet.allOf(StargateKeyring.class)) {
            mapping.put(kr.name, kr);
        }
    }

    public static StargateKeyring byName(String name) {
        return mapping.get(name);
    }    
    
    private String name;    

    private StargateKeyring(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }    
}
