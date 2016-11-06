package com.gnefedev.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public Proxy getPrice(String itemName) {
        return new Proxy(itemName);
    }

    public static class Proxy {
        private final String itemName;

        private Proxy(String itemName) {
            this.itemName = itemName;
        }

        public Proxy withTimeout(int timeout) {
            return this;
        }

        public Integer getResult() {
            return prices.get(itemName);
        }

        public Optional<Integer> async() {
            return Optional.ofNullable(getResult());
        }
    }
}
