package com.gnefedev.specific;

/**
 * Created by gerakln on 07.11.16.
 */
public class MmtProxyFactory {
    public Object createImpl(Class<?> mmtImplClass) {
        try {
            return mmtImplClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
