package xyz.usbpc.simplyrss

import org.apache.commons.codec.binary.Hex
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule

// TODO: This will throw an exception on some date formats... but all RSS feeds I've seen so far use this format...
internal fun String.toDate() : ZonedDateTime = ZonedDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME)
internal fun ZonedDateTime.toHTTPString(): String = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT")).format(this)
internal fun String.md5(): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    val hash = messageDigest.digest(this.toByteArray())
    return Hex.encodeHexString(hash)
}

interface RSSFeedData {
    var hashes: List<String>
    var lastChecked: ZonedDateTime
}

class MemoryRSSFeedData(override var lastChecked: ZonedDateTime = ZonedDateTime.parse("1970-01-01T00:00:00Z") , override var hashes: List<String> = emptyList()) : RSSFeedData

class RSSFeedReader(val url: String, val data: RSSFeedData = MemoryRSSFeedData()) {
    fun getNewItems() : List<RSSItem> {
        val lastChecked = data.lastChecked
        val currentTime = ZonedDateTime.now()
        val result = Jsoup.connect(url)
                .header("If-Modified-Since", lastChecked.toHTTPString())
                .method(Connection.Method.GET)
                .execute()

        if (result.statusCode() == 304) {
            data.lastChecked = currentTime
            return emptyList()
        }
        if (result.statusCode() != 200) throw IllegalStateException("Got status code ${result.statusCode()} with message ${result.statusMessage()}")
        data.lastChecked = currentTime
        //There might be new items
        val channel = result.parse().selectFirst("rss > channel") ?: throw IllegalStateException("Hey, this isn't RSS: $url")
        //If it has a pubDate check if it is was updated after the last time checked if not return without anything
        if (channel.selectFirst("channel > pubDate")?.text()?.toDate()?.isAfter(lastChecked) == false) return emptyList()

        //At this point we either have something new or I hate this RSS feed
        val items = channel.select("channel > item")

        val itemsWithHash = if (items.first().selectFirst("pubDate") != null) {
            val rssChannel = RSSChannel(channel)
            return items.filter { it.selectFirst("pubDate").text().toDate().isAfter(lastChecked) }.map { RSSItem(it, rssChannel) }
        } else if (items.first().selectFirst("guid") != null) {
            items.map { it to it.select("guid").text().md5() }
        } else {
            items.map { it to it.select("link").text().md5() }
        }
        val newElements = itemsWithHash.filter {(_, it) -> it !in data.hashes }.map { (it, _) -> it }
        data.hashes = itemsWithHash.map { (_, it) -> it }
        val rssChannel = RSSChannel(channel)
        return newElements.map { RSSItem(it, rssChannel) }
    }
}

class RSSChannel(val element: Element) {
    fun getElementText(selector: String): String = element.selectFirst(selector)?.text() ?: "null"
}

class RSSItem(val element: Element, val parent: RSSChannel) {
    private companion object {
        val placeholderRegex = Regex("\\\$\\{(.+?)}")
    }
    fun format(formstString: String) : String {
        var prevMatch = 0
        val builder = StringBuilder()
        placeholderRegex.findAll(formstString).forEach { match ->
            builder.append(formstString.subSequence(prevMatch, match.range.start))
            builder.append(getTextFromSelector(match.groups[1]!!.value))
            prevMatch = match.range.endInclusive + 1
        }
        builder.append(formstString.substring(prevMatch))
        return builder.toString()
    }
    private fun getTextFromSelector(selector: String): String {
        val tokens = selector.split(':')
        return if (tokens.size < 2) {
           getElementText(selector)
        } else if (tokens[0] == "channel"){
            parent.getElementText(tokens[1])
        } else if (tokens[0] == "item"){
            getElementText(tokens[1])
        } else {
            "null"
        }
    }
    fun getElementText(selector: String): String = element.selectFirst(selector)?.text() ?: "null"
}

class RSSReaderTimer(val defaultDelay: Int = 15) {
    private val timer = Timer()
    fun addFeed(reader: RSSFeedReader, delay: Int = defaultDelay, block: (RSSItem) -> Unit) {
        timer.schedule(delay*1000*60L, 15*1000*60) {
            reader.getNewItems().forEach(block)
        }
    }
}