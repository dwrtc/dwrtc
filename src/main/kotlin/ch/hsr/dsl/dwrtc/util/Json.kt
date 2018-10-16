package ch.hsr.dsl.dwrtc.util

import io.javalin.json.JavalinJackson


inline fun <reified OutputType> jsonTo(jsonString: String) =
    JavalinJackson.fromJson(jsonString, OutputType::class.java)

fun toJson(message: Any) = JavalinJackson.toJson(message)
