package com.gnefedev.flux.cats.model

import java.io.Serializable

data class Cat(
    val id: Int = -1,
    val name: String,
    val rating: Int,
    val ownerId: Int
) : Serializable