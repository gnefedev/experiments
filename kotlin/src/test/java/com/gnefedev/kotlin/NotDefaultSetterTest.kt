package com.gnefedev.kotlin

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by gerakln on 10.06.17.
 */
class NotDefaultSetterTest {
    @Test fun customSetter() {
        val ivan = User("Ivan")
        assertEquals("Ivan", ivan.name)
        ivan.name = "Ivan"
        assertEquals("IVAN", ivan.name)
    }

    class User(
            nameParam: String
    ) {
        var name: String = nameParam
            set(value) {
                field = value.toUpperCase()
            }
    }
}