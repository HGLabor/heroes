package gg.norisk.heroes.common.ffa.experience

object ExperienceRegistry {
    val reasons = mutableSetOf<ExperienceReason>()

    val KILLED_PLAYER = register("killed_player", 500)
    val PLAYER_DEATH = register("player_death", 25)
    val SOUP_EATEN = register("soup_eaten", 5)
    val SMALL_ABILITY_USE = register("small_ability_use", 5)
    val RECRAFT = register("soup_recraft", 5)
    val END_KILL_STREAK = register("end_kill_streak", 1000)
    val DEALING_DAMAGE = register("dealing_damage", 1)
    val TAKING_DAMAGE = register("taking_damage", 1)
    val IDLE = register("idle", 1)

    fun register(key: String, value: Int): ExperienceReason {
        return ExperienceReason(key, value).apply {
            reasons.add(this)
        }
    }
}
