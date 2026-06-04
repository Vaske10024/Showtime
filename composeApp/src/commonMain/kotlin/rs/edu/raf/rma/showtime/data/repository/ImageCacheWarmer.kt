package rs.edu.raf.rma.showtime.data.repository

import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult

class ImageCacheWarmer(
    private val platformContext: PlatformContext,
) {
    suspend fun warm(urls: List<String>): Int {
        val imageLoader = SingletonImageLoader.get(platformContext)
        return urls.distinct().count { url ->
            val request = ImageRequest.Builder(platformContext)
                .data(url)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .build()

            runCatching { imageLoader.execute(request) }
                .getOrNull() is SuccessResult
        }
    }
}
