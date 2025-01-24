package gg.norisk.heroes.toph.network

import gg.norisk.heroes.toph.TophManager.toId
import gg.norisk.heroes.toph.ability.EarthColumnDescription
import net.silkmc.silk.network.packet.c2sPacket

val earthColumnBlockInfos = c2sPacket<EarthColumnDescription>("earth_column_block_infos".toId())
