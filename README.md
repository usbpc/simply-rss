# simply-rss - a simple RSS Feed reader for Kotlin

[![](https://jitpack.io/v/xyz.usbpc/simply-rss.svg)](https://jitpack.io/#xyz.usbpc/simply-rss)

Builds for this library are available on [jitpack](https://jitpack.io/#xyz.usbpc/simply-rss).

# Usage
The main thing to use is the RSSFeedReader class.

To read the Hello Internet Potcast RSS feed:
````kotlin
fun main(args: Array<String>) {
    val reader = RSSFeedReader("http://www.hellointernet.fm/podcast?format=rss")

    reader.getNewItems().forEach {
        println(it.format("\${channel:title} just published \${title}"))
    }
}
````

This will print "Hello Internet just published <episode title>" for every episode published so far.

If you let the program run and call `RSSFeedReader::getNewItems` again it will not print anything, if you wait long enough for a new episode to be released it will then only print that one episode.

To make the Data about what is already seen persistent you can also specify the place where the data is stored:

```kotlin
interface RSSFeedData {
    var hashes: List<String>
    var lastChecked: ZonedDateTime
}
```

You can use that as follows:
```kotlin
class DBRSSFeedData: RssFeedData {
//...
}

fun main(args: Array<String>) {
    val reader = RSSFeedReader("http://www.hellointernet.fm/podcast?format=rss", DBRSSFeedData())

    reader.getNewItems().forEach {
        println(it.format("\${channel:title} just published \${title}"))
    }
}
```

## Formatting of `RssItem::format(String)`

Templates with the format of `${selector}` will be replaced with the text of the corresponding text of the xml element from the current rss Item.

Using `${channel:selector}` will be replaced with the text of the corresponding text of the rss channel's coresponding xml element.

The selectors have to follow the format defined [here](https://jsoup.org/cookbook/extracting-data/selector-syntax).