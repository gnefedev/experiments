package com.gnefedev.sprcific

/**
 * Created by gerakln on 06.11.16.
 */
fun getSyncResult(proxy: Any): Int? {
    return (proxy as MmtProxy).result
}

fun getMmtImplementationClass(apiClientInterface: Class<*>): Class<*> {
    return Class.forName(getApiInterface(apiClientInterface).`package`.name + ".MmtImplementation")
}

private fun getApiInterface(apiClientInterface: Class<*>): Class<*> {
    for (apiInterface in apiClientInterface.interfaces) {
        if (apiInterface.isAnnotationPresent(Api::class.java)) {
            return apiInterface
        }
    }
    throw IllegalArgumentException("$apiClientInterface не наследует @Api интеофейса")
}