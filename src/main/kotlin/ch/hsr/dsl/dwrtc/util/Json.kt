package ch.hsr.dsl.dwrtc.util

import io.javalin.json.JavalinJackson

/**
 * Convert JSON to `OutputType`
 */
inline fun <reified OutputType> jsonTo(jsonString: String) =
        JavalinJackson.fromJson(jsonString, OutputType::class.java)

/**
 * Convert to JSON
 */
fun toJson(message: Any) = JavalinJackson.toJson(message)
