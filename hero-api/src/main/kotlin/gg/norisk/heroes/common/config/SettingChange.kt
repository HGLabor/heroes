import kotlinx.serialization.Serializable

@Serializable
data class SettingChange(
    val configKey: String,
    val settingKey: String,
    val newValue: String,
    val type: String,
)