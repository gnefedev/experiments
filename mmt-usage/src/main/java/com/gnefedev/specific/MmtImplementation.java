package com.gnefedev.specific;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerakln on 06.11.16.
 */
public class MmtImplementation {
    private static final Map<String, Integer> prices;

    static {
        prices = new HashMap<>();
        prices.put("pan", 10);
        prices.put("monitor", 1000);
    }

    public MmtProxy getPrice(final String itemName) {
        return new MmtProxy() {
            @Override
            public Integer getResult()  {
                return MmtImplementation.prices.get(itemName);
            }
        };
    }

}
