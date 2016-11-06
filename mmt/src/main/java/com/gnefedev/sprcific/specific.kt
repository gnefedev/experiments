package com.gnefedev.sprcific

/**
 * Created by gerakln on 06.11.16.
 */
fun getSyncResult(proxy: Any, timeout: Int): Int? {
    return (proxy as MmtProxy).withTimeout(timeout).result
}

fun getAsyncResult(proxy: Any): MmtProxy.Holder {
    return (proxy as MmtProxy).async()
}

fun getMmtImplementationClass(apiClientInterface: Class<*>): Class<*> {
    return Class.forName(getApiInterface(apiClientInterface).`package`.name + ".MmtImplementation")
}

fun getApiInterface(apiClientInterface: Class<*>): Class<*> {
    for (apiInterface in apiClientInterface.interfaces) {
        if (apiInterface.isAnnotationPresent(Api::class.java)) {
            return apiInterface
        }
    }
    throw IllegalArgumentException("$apiClientInterface не наследует @Api интеофейса")
}