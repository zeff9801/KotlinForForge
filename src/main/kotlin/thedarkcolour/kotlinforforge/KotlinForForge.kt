package thedarkcolour.kotlinforforge

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager

/**
 * Set `modLoader` in mods.toml to
 * `"kotlinforforge"` and loaderVersion to `"[1.16,)"`.
 *
 * Make sure to use [KotlinModLoadingContext]
 * instead of [FMLJavaModLoadingContext].
 *
 * For a more detailed example mod,
 * check out the [KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton).
 */
@Mod("kotlinforforge")
public object KotlinForForge {
    private val LOGGER = LogManager.getLogger()
    init {
        LOGGER.info("Kotlin For Forge Enabled!")
    }
}