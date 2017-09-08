package com.epl.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.epl.akka.WebCrawler.{CrawlRequest, CrawlResponse}

object Main extends App {
  val system = ActorSystem("Crawler")
  val webCrawler = system.actorOf(Props[WebCrawler], "WebCrawler")
  val main = system.actorOf(Props[Main](new Main(webCrawler, "https://www.premierleague.com/", 1)), "PLActor")
}

class Main(receptionist: ActorRef, url: String, depth: Integer) extends Actor {
  receptionist ! CrawlRequest(url, depth)
  def receive = {
    case CrawlResponse(root, links) =>

      links.foreach(
        url => {
          context.actorOf(Props[HTMLParser](new HTMLParser(url, 1)))
        }
      )
  }
}