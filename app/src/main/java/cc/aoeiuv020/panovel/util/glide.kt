package cc.aoeiuv020.panovel.util

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.InputStream
import java.net.URL


/**
 * Created by AoEiuV020 on 2018.06.18-17:20:10.
 */
@Suppress("DEPRECATION")
@Deprecated("glide说这种用法过时了，")
class JarGlideModule : com.bumptech.glide.module.GlideModule, AnkoLogger {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Do nothing.
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        info { "Glide registerComponents: JarGlideModule" }
        registry.prepend(GlideUrl::class.java, InputStream::class.java, JarFactory())
    }
}

/*
@GlideModule
class JarLibraryGlideModule : LibraryGlideModule(), AnkoLogger {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        info { "Glide registerComponents: JarGlideModule" }
        registry.append(String::class.java, InputStream::class.java, JarFactory())
    }
}
*/

private class JarFactory : ModelLoaderFactory<GlideUrl, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory?): ModelLoader<GlideUrl, InputStream> {
        return JarLoader()
    }

    override fun teardown() {
        // 不知道要不要做什么，
    }
}

private class JarLoader : ModelLoader<GlideUrl, InputStream> {
    override fun buildLoadData(model: GlideUrl, width: Int, height: Int, options: Options?): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(ObjectKey(model), UrlStreamFetcher(model.toURL()))
    }

    override fun handles(model: GlideUrl): Boolean {
        return try {
            model.toURL().protocol == "jar"
        } catch (e: Exception) {
            false
        }
    }
}

private class UrlStreamFetcher(
        private val model: URL
) : DataFetcher<InputStream> {
    private lateinit var inputStream: InputStream
    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun cleanup() {
        if (::inputStream.isInitialized) {
            try {
                inputStream.close()
            } catch (e: Exception) {
            }
        }
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }

    override fun cancel() {
    }

    override fun loadData(priority: Priority?, callback: DataFetcher.DataCallback<in InputStream>) {
        try {
            inputStream = model.openStream()
        } catch (e: Exception) {
            callback.onLoadFailed(e)
            return
        }
        // 不清楚流程，但是以防万一，模仿HttpUrlFetcher把成功的回调放在外面，
        callback.onDataReady(inputStream)
    }
}