package com.gnefedev.sprcific;

/**
 * Created by gerakln on 06.11.16.
 */
public abstract class MmtProxy {
    public MmtProxy withTimeout(int timeout) {
        return this;
    }

    public abstract Integer getResult();

    public Holder async() {
        return new Holder(getResult());
    }

    public static class Holder {
        private final Integer result;

        private Holder(Integer result) {
            this.result = result;
        }

        public Integer get() {
            return result;
        }
    }
}
