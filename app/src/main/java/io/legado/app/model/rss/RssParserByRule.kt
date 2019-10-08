package io.legado.app.model.rss

import io.legado.app.App
import io.legado.app.R
import io.legado.app.data.entities.RssArticle
import io.legado.app.data.entities.RssSource
import io.legado.app.model.analyzeRule.AnalyzeRule
import retrofit2.Response

object RssParserByRule {

    @Throws(Exception::class)
    fun parseXML(response: Response<String>, rssSource: RssSource): MutableList<RssArticle> {

        val xml = response.body()
        if (xml.isNullOrBlank()) {
            throw Exception(
                App.INSTANCE.getString(
                    R.string.error_get_web_content,
                    rssSource.sourceUrl
                )
            )
        }
        var ruleArticles = rssSource.ruleArticles
        if (ruleArticles.isNullOrBlank()) {
            return RssParser.parseXML(xml, rssSource.sourceUrl)
        } else {
            val articleList = mutableListOf<RssArticle>()
            val analyzeRule = AnalyzeRule()
            analyzeRule.setContent(xml)
            var reverse = false
            if (ruleArticles.startsWith("-")) {
                reverse = true
                ruleArticles = ruleArticles.substring(1)
            }
            val collections = analyzeRule.getElements(ruleArticles)
            val ruleTitle = analyzeRule.splitSourceRule(rssSource.ruleTitle ?: "")
            val ruleAuthor = analyzeRule.splitSourceRule(rssSource.ruleAuthor ?: "")
            val rulePubDate = analyzeRule.splitSourceRule(rssSource.rulePubDate ?: "")
            val ruleCategories = analyzeRule.splitSourceRule(rssSource.ruleCategories ?: "")
            val ruleDescription = analyzeRule.splitSourceRule(rssSource.ruleDescription ?: "")
            val ruleImage = analyzeRule.splitSourceRule(rssSource.ruleImage ?: "")
            val ruleContent = analyzeRule.splitSourceRule(rssSource.ruleContent ?: "")
            val ruleLink = analyzeRule.splitSourceRule(rssSource.ruleLink ?: "")
            for ((index, item) in collections.withIndex()) {
                getItem(
                    item,
                    analyzeRule,
                    index == 0,
                    ruleTitle,
                    ruleAuthor,
                    rulePubDate,
                    ruleCategories,
                    ruleDescription,
                    ruleImage,
                    ruleContent,
                    ruleLink
                )?.let {
                    it.origin = rssSource.sourceUrl
                    articleList.add(it)
                }
            }
            if (reverse) {
                articleList.reverse()
            }
            for ((index: Int, item: RssArticle) in articleList.withIndex()) {
                item.order = System.currentTimeMillis() + index
            }
            return articleList
        }
    }

    private fun getItem(
        item: Any,
        analyzeRule: AnalyzeRule,
        printLog: Boolean,
        ruleTitle: List<AnalyzeRule.SourceRule>,
        ruleAuthor: List<AnalyzeRule.SourceRule>,
        rulePubDate: List<AnalyzeRule.SourceRule>,
        ruleCategories: List<AnalyzeRule.SourceRule>,
        ruleDescription: List<AnalyzeRule.SourceRule>,
        ruleImage: List<AnalyzeRule.SourceRule>,
        ruleContent: List<AnalyzeRule.SourceRule>,
        ruleLink: List<AnalyzeRule.SourceRule>
    ): RssArticle? {
        val rssArticle = RssArticle()
        analyzeRule.setContent(item)
        rssArticle.title = analyzeRule.getString(ruleTitle)
        rssArticle.author = analyzeRule.getString(ruleAuthor)
        rssArticle.pubDate = analyzeRule.getString(rulePubDate)
        rssArticle.categories = analyzeRule.getString(ruleCategories)
        rssArticle.description = analyzeRule.getString(ruleDescription)
        rssArticle.image = analyzeRule.getString(ruleImage)
        rssArticle.link = analyzeRule.getString(ruleLink)
        rssArticle.content = analyzeRule.getString(ruleContent)
        if (rssArticle.title.isNullOrBlank()) {
            return null
        }
        return rssArticle
    }
}