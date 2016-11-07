package com.gnefedev.test;

import com.gnefedev.AppConfig;
import com.gnefedev.specific.ApiInterface;
import com.gnefedev.usage.ApiInterfaceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by gerakln on 06.11.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ApiUsageTest {
    @Autowired
    private ApiInterface apiInterface;
    @Autowired
    private ApiInterfaceClient apiInterfaceClient;

    @Test
    public void testSimple() {
        assertEquals(10, apiInterface.getPrice("pan").intValue());
        assertEquals(1000, apiInterface.getPrice("monitor").intValue());
        assertNull(apiInterface.getPrice("book"));

        assertEquals(10, apiInterfaceClient.getPrice("pan").intValue());
        assertEquals(1000, apiInterfaceClient.getPrice("monitor").intValue());
        assertNull(apiInterfaceClient.getPrice("book"));

        assertEquals(10, apiInterfaceClient.getPriceAsync("pan").get().intValue());
    }
}
