package com.coconutplace.wekit.utils

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.util.Preconditions


class GlideUrlWithCacheKey(url: String, cacheKey: String) : GlideUrl(url) {
    private val url: String
    private val cacheKey: String?

    override fun getCacheKey(): String? {
        return cacheKey
    }

    override fun toString(): String {
        return url
    }

    init {
        Preconditions.checkNotNull(url)
        Preconditions.checkNotEmpty(url)
        Preconditions.checkNotNull(cacheKey)
        Preconditions.checkNotEmpty(cacheKey)
        this.url = url
        this.cacheKey = cacheKey
    }
}