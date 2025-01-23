package gg.norisk.heroes.common.config

import SettingChange
import gg.norisk.heroes.common.networking.Networking
import net.silkmc.silk.core.Silk
import kotlin.reflect.KProperty

abstract class ConfigNode(val name: String) {
    val key = name.lowercase().replace(' ', '_')
    val settings = hashMapOf<String, SettingDelegate<out Any>>()

    abstract inner class SettingDelegate<T : Any> {
        abstract val defaultValue: T
        var value: T? = null
        lateinit var settingKey: String

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return get()
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            set(newValue)
        }

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): SettingDelegate<T> {
            settingKey = property.name
            settings[settingKey] = this
            return this
        }

        fun set(any: Any?) {
            value = any as T
            val settingChange = SettingChange(key, settingKey, value!!.toString(), defaultValue::class.simpleName!!)
            if (Silk.server?.playerManager == null) return
            Networking.s2cSettingChangePacket.sendToAll(settingChange)
        }

        fun get(): T {
            if (value == null) value = defaultValue
            return value ?: defaultValue
        }
    }

    inline fun <reified T : Any> any(default: T) = object : SettingDelegate<T>() {
        override val defaultValue = default
    }

    fun boolean(default: Boolean) = any(default)
    fun int(default: Int) = any(default)
    fun long(default: Long) = any(default)
    fun float(default: Float) = any(default)
    fun double(default: Double) = any(default)
    fun string(default: String) = any(default)
}
