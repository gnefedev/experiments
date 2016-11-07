package com.gnefedev.usage;

import com.gnefedev.mmt.ApiClient;
import com.gnefedev.mmt.Timeout;
import com.gnefedev.specific.ApiInterface;
import com.gnefedev.specific.MmtProxy;

/**
 * Created by gerakln on 06.11.16.
 */
@ApiClient
public interface ApiInterfaceClient extends ApiInterface {
    @Timeout(60)
    Integer getPrice(String itemName);
    MmtProxy.Holder getPriceAsync(String itemName);
}
