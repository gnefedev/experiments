package com.gnefedev.kotlin

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by gerakln on 17.06.17.
 */
val SEPTEMBER_1990: Date = SimpleDateFormat("yyyy-MM").parse("1990-09")

class DefaultParameters {
    val userDto: UserDTO = UserDTO("Ivan", SEPTEMBER_1990, Date(), false)

    fun usageVersion1() {
        val newUser = User("Ivan", SEPTEMBER_1990)
        val userFromDto = User(userDto.name, userDto.birthDate, userDto.created)
    }

    fun usageVersion2() {
        val newUser = User("Ivan", SEPTEMBER_1990)
        val userFromDto = User(userDto.name, userDto.birthDate, userDto.created, userDto.disabled)
    }

    data class User (
            val name: String,
            val birthDate: Date,
            val created: Date = Date(),
            val disabled: Boolean = false
    )

    data class UserDTO (
            val name: String,
            val birthDate: Date,
            val created: Date,
            val disabled: Boolean
    )
}