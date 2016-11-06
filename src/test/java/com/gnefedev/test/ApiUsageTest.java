package com.gnefedev.test;

import com.gnefedev.AppConfig;
import com.gnefedev.api.ApiInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by gerakln on 06.11.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ApiUsageTest {
    @Autowired
    private ApiInterface apiInterface;

    @Test
    public void testSimple() {
        assertEquals(10, apiInterface.getPrice("pan").intValue());
        assertEquals(1000, apiInterface.getPrice("monitor").intValue());
        assertNull(apiInterface.getPrice("book"));
    }
}
