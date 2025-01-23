package gg.norisk.heroes.common.utils

import com.google.common.annotations.VisibleForTesting
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.HeroesManager.isServer
import gg.norisk.heroes.common.HeroesManager.toId
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.registry.VersionedIdentifier
import net.minecraft.resource.*
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.resource.metadata.PackFeatureSetMetadata
import net.minecraft.resource.metadata.PackResourceMetadata
import net.minecraft.resource.metadata.ResourceMetadataMap
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.path.SymlinkFinder
import java.nio.file.Path
import java.util.*

class HeroDataPackProvider(symlinkFinder: SymlinkFinder?) : VanillaResourcePackProvider(
    ResourceType.SERVER_DATA,
    createDefaultPack(),
    ID,
    symlinkFinder
) {
    override fun getDisplayName(string: String): Text = Text.literal(string)

    override fun createDefault(resourcePack: ResourcePack): ResourcePackProfile? {
        return ResourcePackProfile.create(
            INFO,
            createPackFactory(resourcePack),
            ResourceType.SERVER_DATA,
            BOTTOM_POSITION
        )
    }

    override fun create(
        string: String,
        packFactory: ResourcePackProfile.PackFactory,
        text: Text
    ): ResourcePackProfile? {
        return ResourcePackProfile.create(createInfo(string, text), packFactory, ResourceType.SERVER_DATA, TOP_POSITION)
    }

    companion object {
        private val METADATA = PackResourceMetadata(
            Text.translatable("dataPack.hero.description"),
            SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA),
            Optional.empty()
        )
        private val FEATURE_FLAGS = PackFeatureSetMetadata(FeatureSet.of(HeroesManager.heroesFlag))
        private val METADATA_MAP: ResourceMetadataMap = ResourceMetadataMap.of(
            PackResourceMetadata.SERIALIZER,
            METADATA,
            PackFeatureSetMetadata.SERIALIZER,
            FEATURE_FLAGS
        )
        private val INFO: ResourcePackInfo
        private val BOTTOM_POSITION: ResourcePackPosition
        private val TOP_POSITION: ResourcePackPosition
        private val ID: Identifier

        private fun createInfo(string: String, text: Text): ResourcePackInfo {
            return ResourcePackInfo(string, text, source, Optional.of(createHero(string)))
        }

        private fun createHero(string: String): VersionedIdentifier {
            return VersionedIdentifier(HeroesManager.MOD_ID, string, SharedConstants.getGameVersion().id)
        }

        @VisibleForTesting
        fun createDefaultPack(): DefaultResourcePack {
            return DefaultResourcePackBuilder()
                .withMetadataMap(METADATA_MAP)
                .withNamespaces(HeroesManager.MOD_ID)
                .runCallback()
                .withDefaultPaths()
                .build(INFO)
        }

        fun createManager(path: Path?, symlinkFinder: SymlinkFinder?): ResourcePackManager {
            return ResourcePackManager(
                HeroDataPackProvider(symlinkFinder),
                FileResourcePackProvider(path, ResourceType.SERVER_DATA, ResourcePackSource.WORLD, symlinkFinder)
            )
        }

        val source get() = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) ResourcePackSource.SERVER else ResourcePackSource.FEATURE

        init {
            INFO = ResourcePackInfo(
                "hero",
                Text.translatable("dataPack.hero.name"),
                source,
                Optional.of(createHero("core"))
            )
            BOTTOM_POSITION = ResourcePackPosition(false, ResourcePackProfile.InsertionPosition.BOTTOM, false)
            TOP_POSITION = ResourcePackPosition(false, ResourcePackProfile.InsertionPosition.TOP, false)
            ID = "heroes".toId()
        }
    }
}
