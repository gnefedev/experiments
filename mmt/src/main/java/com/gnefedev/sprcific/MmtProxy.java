package com.gnefedev.sprcific;

/**
 * Created by gerakln on 06.11.16.
 */
public abstract class MmtProxy {
    private final String itemName;

    MmtProxy(String itemName) {
        this.itemName = itemName;
    }

    public MmtProxy withTimeout(int timeout) {
        return this;
    }

    public abstract Integer getResult();

    public Holder async() {
        return new Holder(getResult());
    }

    private static class Holder {
        private final Integer result;

        private Holder(Integer result) {
            this.result = result;
        }

        public Integer get() {
            return result;
        }
    }
}
