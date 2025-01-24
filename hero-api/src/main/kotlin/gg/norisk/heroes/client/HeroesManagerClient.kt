package gg.norisk.heroes.client

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
import gg.norisk.heroes.common.HeroesManager.logger
import net.fabricmc.api.ClientModInitializer

object HeroesManagerClient : ClientModInitializer {
    override fun onInitializeClient() {
        logger.info("Init Hero client...")

        HeroKeyBindings.initClient()
        ConfigManagerClient.init()
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
