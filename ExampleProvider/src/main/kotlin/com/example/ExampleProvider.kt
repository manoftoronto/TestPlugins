package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class ExampleProvider(val plugin: TestPlugin) : MainAPI() {
    override var mainUrl = "https://freeporn.gg"
    override var name = "Freeporn"
    override val hasMainPage = true
    override var lang = "en"
    override val supportedTypes = setOf(TvType.NSFW, TvType.Movie)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/page/$page/").document
        val home = document.select("div.video-item, div.item, article").mapNotNull { it.toSearchResult() }
        return newHomePageResponse("Home", home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("a.title, h3 a, .video-title, a")?.attr("title")
            ?: this.selectFirst("a.title, h3 a, .video-title")?.text()
            ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(
            this.selectFirst("img")?.attr("data-src") ?: this.selectFirst("img")?.attr("src")
        )
        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.video-item, div.item, article").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: "Video"
        val poster = document.selectFirst("meta[property='og:image']")?.attr("content")

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val videoUrl = document.selectFirst("video source")?.attr("src")
            ?: document.selectFirst("video")?.attr("src")
            ?: document.selectFirst("iframe")?.attr("src")
        if (videoUrl != null) {
            loadExtractor(videoUrl, data, subtitleCallback, callback)
        }
        return true
    }
}
