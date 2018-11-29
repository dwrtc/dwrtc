package test

import ch.hsr.dsl.dwrtc.util.config
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

val TEST_VALUE = Key("test.value", intType)

class ConfigTest : WordSpec() {

    init {
        "a config value" should {
            assertAll { int: Int ->
                val propertySetter = PropertySetter("test.value", int.toString())
                propertySetter.set()

                "be read properly" {
                    config[TEST_VALUE].shouldBe(int)
                }
                propertySetter.reset()
            }
        }
    }
}
