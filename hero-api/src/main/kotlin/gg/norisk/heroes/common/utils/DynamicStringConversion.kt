package gg.norisk.heroes.common.utils

import java.lang.reflect.Method
import kotlin.reflect.KClass

object DynamicStringConversion {
    private val conversionFunction = hashMapOf<String, Method>()

    fun convertString(valueAsString: String, type: String): Any {
        runCatching {
            val typeName = type.lowercase()
            if (typeName == "string") return valueAsString

            val stringsKtClass = Class.forName("kotlin.text.StringsKt")
            val function = conversionFunction.computeIfAbsent(typeName) {
                stringsKtClass.methods.first {
                    it.name.startsWith("to$typeName", true) && it.parameterCount == 1
                }
            }
            function.isAccessible = true
            function.invoke(null, valueAsString)
        }.onSuccess {
            return it
        }

        throw IllegalArgumentException("Can not convert settings to type $type")
    }

    fun convertString(valueAsString: String, type: KClass<*>): Any {
        return convertString(valueAsString, type.simpleName!!)
    }

    inline fun <reified T> convertString(valueAsString: String): Any {
        return convertString(valueAsString, T::class.simpleName!!)
    }
}