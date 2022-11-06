package com.jokku.jokeapp.data.entity

import com.google.gson.annotations.SerializedName
import com.jokku.jokeapp.core.Mapper

class NewJokeServerModel(
    @SerializedName("id")
    private val id: Int,
    @SerializedName("setup")
    private val setup: String,
    @SerializedName("delivery")
    private val punchline: String
) : Mapper<JokeDataModel> {

    override fun map() = JokeDataModel(id, setup, punchline)
}