package gg.norisk.heroes.client

import gg.norisk.heroes.client.animations.AnimationManagerClient
import gg.norisk.heroes.client.command.ClientHeroCommand
import gg.norisk.heroes.client.config.ConfigManagerClient
import gg.norisk.heroes.client.hero.ability.AbilityKeyBindManager
import gg.norisk.heroes.client.hero.ability.AbilityManagerClient
import gg.norisk.heroes.client.networking.MouseListener
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.CameraShaker
import gg.norisk.heroes.client.renderer.KeyBindHud
import gg.norisk.heroes.client.renderer.Speedlines
import gg.norisk.heroes.client.ui.OrthoCamera
import gg.norisk.heroes.common.HeroesManager.abilityManagers
import gg.norisk.heroes.common.HeroesManager.animationManagers
import gg.norisk.heroes.common.HeroesManager.configManagers
import gg.norisk.heroes.common.HeroesManager.logger
import net.fabricmc.api.ClientModInitializer

object HeroesManagerClient : ClientModInitializer {
    override fun onInitializeClient() {
        logger.info("Init Hero client...")
        configManagers.add(ConfigManagerClient)
        animationManagers.add(AnimationManagerClient)
        abilityManagers.add(AbilityManagerClient)

        HeroKeyBindings.initClient()
        ConfigManagerClient.init()
        AnimationManagerClient.init()
        AbilityManagerClient.init()
        OrthoCamera.initClient()
        AbilityKeyBindManager.initializeKeyBindListeners()

        KeyBindHud.init()
        MouseListener.initClient()
        Speedlines.initClient()
        CameraShaker.initClient()
        ClientHeroCommand.init()
    }
}
