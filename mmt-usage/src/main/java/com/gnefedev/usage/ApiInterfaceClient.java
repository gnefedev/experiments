package com.gnefedev.usage;

import com.gnefedev.mmt.Timeout;
import com.gnefedev.sprcific.ApiInterface;
import com.gnefedev.mmt.ApiClient;

/**
 * Created by gerakln on 06.11.16.
 */
@Timeout(60)
@ApiClient
public interface ApiInterfaceClient extends ApiInterface {
}
