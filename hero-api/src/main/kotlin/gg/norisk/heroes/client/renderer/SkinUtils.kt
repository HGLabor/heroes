package gg.norisk.heroes.client.renderer

import com.mojang.blaze3d.systems.RenderSystem
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.getHero
import gg.norisk.heroes.common.mixin.accessor.NativeImageAccessor
import gg.norisk.heroes.common.mixin.accessor.TextureManagerAccessor
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import java.io.File
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

object SkinUtils {
    fun initClient() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) return
    }

    fun applyOverlay(baseSkinId: Identifier, overlayId: Identifier, hero: Hero) {
        val result = combineSkins(baseSkinId, overlayId)
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            logger.info("Merged Skin $baseSkinId $overlayId: $result")
            val folder = File(FabricLoader.getInstance().configDir.toFile(), "aang").apply { mkdirs() }

            //MinecraftClient.getInstance().textureManager.registerTexture()

            result?.writeTo(File(folder, "${baseSkinId.path}_overlay.png"))
        }

        if (result != null) {
            MinecraftClient.getInstance().submit {
                MinecraftClient.getInstance().textureManager.registerTexture(
                    baseSkinId.toOverlaySkin(hero.internalKey.lowercase()),
                    NativeImageBackedTexture(result)
                )
            }
        }
    }

    fun Identifier.toOverlaySkin(id: String): Identifier {
        return Identifier.of(this.namespace, this.path + "_$id")
    }

    fun redirectCombinedSkin(original: Identifier, player: AbstractClientPlayerEntity?): Identifier {
        val hero = player?.getHero()
        val skin = hero?.overlaySkin
        if (hero != null && skin != null) {
            val mergedSkin = original.toOverlaySkin(hero.internalKey.lowercase())
            if ((MinecraftClient.getInstance().textureManager as TextureManagerAccessor).textures.getOrDefault(
                    mergedSkin,
                    null
                ) != null
            ) {
                return mergedSkin
            } else {
                applyOverlay(original, skin, hero)
                return original
            }
        } else {
            return original
        }
    }

    fun extractTextureToNativeImage(glId: Int, width: Int, height: Int): NativeImage {
        // Erstelle ein NativeImage mit der passenden Breite, Höhe und Format
        val nativeImage = NativeImage(NativeImage.Format.RGBA, width, height, false)

        // Binde die Textur mit der OpenGL-ID
        RenderSystem.bindTexture(glId)

        // Lade die Textur-Pixel-Daten aus OpenGL in das NativeImage
        nativeImage.loadFromTextureImage(0, false) // i = 0 für Mipmap Level 0, bl = false für keine Alpha-Korrektur

        return nativeImage // Das NativeImage enthält nun die Texturdaten
    }

    /**
     * Kombiniert zwei Skins, wobei der zweite Skin alles ersetzt, was nicht transparent ist,
     * und gibt das resultierende Bild als NativeImage zurück.
     *
     * @param baseSkin Der Identifier für das Basisbild (der erste Skin).
     * @param overlaySkin Der Identifier für das Overlay-Bild (der zweite Skin).
     * @return Das kombinierte Bild als NativeImage.
     */
    fun combineSkins(baseSkin: Identifier, overlaySkin: Identifier): NativeImage? {
        return try {
            // Lade die Basis- und Overlay-Skins als NativeImage
            val baseImageTexture =
                (MinecraftClient.getInstance().textureManager as TextureManagerAccessor).textures.getOrDefault(
                    baseSkin,
                    null
                )

            val overlayImage = loadSkinAsNativeImage(overlaySkin)

            // Falls einer der beiden Skins nicht geladen werden konnte
            if (baseImageTexture == null || overlayImage == null) {
                return null
            }

            //ich hoffe das macht nichts kapuut lol
            val baseImage: NativeImage = extractTextureToNativeImage(baseImageTexture.glId, 64, 64)

            // Überprüfen, ob beide Bilder die gleiche Größe haben
            if (baseImage.width != overlayImage.width || baseImage.height != overlayImage.height) {
                return null
            }

            // Erstelle ein neues NativeImage mit der gleichen Größe wie die Basis
            val combinedImage = NativeImage(baseImage.width, baseImage.height, true)

            // Durchlaufen der Pixel und anwenden des Overlays auf das neue Bild
            for (x in 0 until baseImage.width) {
                for (y in 0 until baseImage.height) {
                    val dummy = baseImage as NativeImageAccessor
                    val dummy2 = overlayImage as NativeImageAccessor
                    val dummy3 = combinedImage as NativeImageAccessor

                    val baseColor = baseImage.invokeGetColor(x, y)
                    val overlayColor = overlayImage.invokeGetColor(x, y)
                    val alpha = (overlayColor shr 24) and 0xFF  // Alpha-Wert extrahieren

                    // Wenn das Overlay-Pixel nicht transparent ist, kopiere es ins kombinierte Bild
                    if (alpha > 0) {
                        combinedImage.invokeSetColor(x, y, overlayColor)
                    } else {
                        // Andernfalls kopiere das Basis-Pixel
                        combinedImage.invokeSetColor(x, y, baseColor)
                    }
                }
            }

            // Freigeben der Basis- und Overlay-Bilder, da sie nicht mehr benötigt werden
            baseImage.close()
            overlayImage.close()

            // Gib das kombinierte Bild zurück
            combinedImage

        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Lädt einen Skin von einem Identifier als NativeImage.
     *
     * @param skinIdentifier Der Identifier des Skins.
     * @return Das NativeImage des Skins, oder null falls das Bild nicht geladen werden konnte.
     */
    fun loadSkinAsNativeImage(skinIdentifier: Identifier): NativeImage? {
        val resourceManager = MinecraftClient.getInstance().resourceManager
        return try {
            val resource = resourceManager.getResource(skinIdentifier).getOrNull() ?: return null
            NativeImage.read(resource.inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
