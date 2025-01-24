package gg.norisk.heroes.katara.entity

import gg.norisk.heroes.katara.utils.EntityCircleTracker
import gg.norisk.heroes.katara.utils.EntitySpinTracker
import kotlinx.coroutines.Job

interface IKataraEntity {
    var katara_waterHealingJob: Job?
    val katara_entitySpinTracker: EntitySpinTracker
    val katara_entityCircleTracker: EntityCircleTracker
}