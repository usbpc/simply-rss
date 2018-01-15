import org.apache.commons.codec.binary.Hex
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule


class RSSReader(val userAgent: String = "RSSReader", val respectTTL:Boolean = true) {
    private val sheduler = Timer()

    /**
     * Schedules the feed to be read every delay minutes first looking for updates when this methode is called
     */
    /*fun schedule(feed: RSSFeed, delay: Int, callback: (List<Element>) -> Unit) {
        sheduler.schedule(delay * 1000L * 60L, delay * 1000L * 60L) {
            callback(feed.getNewItems())
        }
        callback(feed.getNewItems())
    }*/
}

fun main(args: Array<String>) {

    /*val feed = rssFeedFromURL("http://www.hellointernet.fm/podcast?format=rss")
    feed.lastChecked = ZonedDateTime.now().minusDays(7)
    feed.getNewItems().forEach {
        val item = RSSItem(it)
        println(item.format("[\${item:title}](\${item:link}) has been published by [\${channel:title}](\${channel:link})!"))
    }*/
}

