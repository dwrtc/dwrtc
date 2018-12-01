package test

import ch.hsr.dsl.dwrtc.util.jsonTo
import ch.hsr.dsl.dwrtc.util.toJson
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec

data class X(val y: String, val z: Int)
data class XNull(val y: String?, val z: Int?)

class JsonConversionTest : WordSpec() {

    init {
        "a class" should {

            val x = X("Hello", 500)
            val json = toJson(x)
            val expected = """{"y":"Hello","z":500}"""

            "serialize correctly" {
                json.shouldBe(expected)
            }
        }
        "a nullable class" should {

            val xNull = XNull(null, null)
            val json = toJson(xNull)
            val expected = """{"y":null,"z":null}"""

            "serialize correctly" {
                json.shouldBe(expected)
            }
        }

        "a correct JSON" should {

            val json = """{"y":"Hello","z":500}"""
            val expected = X("Hello", 500)
            val x = jsonTo<X>(json)

            "deserialize correctly" {
                x.shouldBe(expected)
            }
        }
        "a correct JSON of a nullable class" should {

            val json = """{"y":null,"z":null}"""
            val expected = XNull(null, null)
            val xNull = jsonTo<XNull>(json)

            "deserialize correctly" {
                xNull.shouldBe(expected)
            }
        }
        "a JSON with surplus fields" should {

            val json = """{"y":"Hello","z":500,"a":"Test"}"""

            "throw an exception" {
                shouldThrow<UnrecognizedPropertyException> { jsonTo<X>(json) }
            }
        }
        "a JSON with missing fields that are not nullable" should {

            val json = """{"z":500}"""

            "throw an exception" {
                shouldThrow<MissingKotlinParameterException> { jsonTo<X>(json) }
            }
        }
        "a JSON with missing fields that are value types (and have a default value)" should {
            // this is not what we initially expected, but it's default Jackson behavior...
            // https://stackoverflow.com/a/9010622/2616394

            val json = """{"y":"Hello"}"""
            val expected = X("Hello", 0)
            val x = jsonTo<X>(json)

            "assume the default value" {
                x.shouldBe(expected)
            }
        }
    }
}
