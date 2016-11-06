package com.gnefedev.usage;

import com.gnefedev.mmt.ApiClient;
import com.gnefedev.mmt.Timeout;
import com.gnefedev.sprcific.ApiInterface;
import com.gnefedev.sprcific.MmtProxy;

/**
 * Created by gerakln on 06.11.16.
 */
@ApiClient
public interface ApiInterfaceClient extends ApiInterface {
    @Timeout(60)
    Integer getPrice(String itemName);
    MmtProxy.Holder getPriceAsync(String itemName);
}
