package com.gnefedev.kotlin

import org.hibernate.validator.constraints.NotEmpty
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.validation.annotation.Validated
import javax.validation.ConstraintViolationException
import javax.validation.Valid


/**
 * Created by gerakln on 10.06.17.
 */
@SpringBootTest(classes = arrayOf(ConstructorAnnotationsTest.Config::class))
@RunWith(SpringRunner::class)
class ConstructorAnnotationsTest {
    @Autowired private lateinit var validator: Config.Validator

    @Test(expected = ConstraintViolationException::class)
    fun works() {
        validator.validate(UserWithField(""))
    }

    @Test
    fun notWorks() {
        validator.validate(UserWithConstructor(""))
    }

    @Test(expected = ConstraintViolationException::class)
    fun fixed() {
        validator.validate(UserWithFixedConstructor(""))
    }

    @SpringBootApplication
    class Config {
        @Validated @Component
        class Validator {
            fun validate(@Valid any: Any) {}
        }
    }

    class UserWithField(param: String) {
        @NotEmpty var field: String = param
    }

    class UserWithConstructor(
            @NotEmpty var paramAndField: String
    )
    class UserWithFixedConstructor(
            @field:NotEmpty var paramAndField: String
    )
}