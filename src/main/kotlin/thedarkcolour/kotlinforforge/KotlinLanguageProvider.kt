package thedarkcolour.kotlinforforge

import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import java.util.function.Consumer
import org.objectweb.asm.Type
import java.lang.reflect.InvocationTargetException


/**
 * Reuse a bit of code from FMLJavaModLanguageProvider
 */
public class KotlinLanguageProvider : FMLJavaModLanguageProvider() {
    override fun name(): String = "kotlinforforge"

    override fun getFileVisitor(): Consumer<ModFileScanData> {
        return Consumer { scanData ->
            scanData.addLanguageLoader(scanData.annotations.filter { data ->
                data.annotationType == MOD_ANNOTATION
            }.associate { data ->
                val modid = data.annotationData["value"] as String
                val modClass = data.classType.className

                LOGGER.debug(Logging.SCAN, "Found @Mod class $modClass with mod id $modid")
                modid to KotlinModTarget(modClass)
            })
        }
    }

    public class KotlinModTarget(private val className: String) : IModLanguageProvider.IModLanguageLoader {
        override fun <T> loadMod(info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T {
            try {
                val ktContainer = Class.forName("thedarkcolour.kotlinforforge.KotlinModContainer", true, Thread.currentThread().contextClassLoader)
                LOGGER.debug(Logging.LOADING, "Loading KotlinModContainer from classloader ${Thread.currentThread().contextClassLoader} - got ${ktContainer.classLoader}}")

                val constructor = ktContainer.declaredConstructors[0]
                return constructor.newInstance(info, className, modClassLoader, modFileScanResults) as T
            } catch (e: InvocationTargetException) {
                LOGGER.fatal(Logging.LOADING, "Failed to build mod", e)

                val targetException = e.targetException

                if (targetException is ModLoadingException) {
                    throw targetException
                } else {
                    throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e)
                }
            } catch (e: NoSuchMethodException) { // Disadvantages of Kotlin :/
                catastrophe(info, e)
            } catch (e: ClassNotFoundException) {
                catastrophe(info, e)
            } catch (e: InstantiationException) {
                catastrophe(info, e)
            } catch (e: IllegalAccessException) {
                catastrophe(info, e)
            }
        }

        // No need for LambdaExceptionUtils with Kotlin
        private fun catastrophe(info: IModInfo, exception: Exception): Nothing {
            LOGGER.fatal(Logging.LOADING, "Unable to load KotlinModContainer, wat", exception)

            // ModLoadingException
            val mle = Class.forName(
                "net.minecraftforge.fml.ModLoadingException",
                true,
                Thread.currentThread().contextClassLoader
            ) as Class<ModLoadingException>
            // ModLoadingStage
            val mls = Class.forName(
                "net.minecraftforge.fml.ModLoadingStage",
                true,
                Thread.currentThread().contextClassLoader
            ) as Class<ModLoadingStage>

            // Throw a new ModLoadingException containing the exception
            throw mle.getConstructor(
                IModInfo::class.java,
                mls,
                String::class.java,
                Throwable::class.java,
                Array<Any>::class.java
            ).newInstance(
                info,
                java.lang.Enum.valueOf(mls, "CONSTRUCT"),
                "fml.modloading.failedtoloadmodclass",
                exception,
                emptyArray<Any>()
            )
        }
    }
    private companion object {
        private val MOD_ANNOTATION = Type.getType("Lnet/minecraftforge/fml/common/Mod;")
    }
}