package com.epl.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.epl.akka.WebCrawler.{CrawlRequest, CrawlResponse}

object Main extends App {
  println(s"Current Time ${System.currentTimeMillis}")
  val system = ActorSystem("Crawler")
  val webCrawler = system.actorOf(Props[WebCrawler], "WebCrawler")
  val main = system.actorOf(Props[Main](new Main(webCrawler, "https://www.premierleague.com/", 1)), "BBCActor")
}

class Main(receptionist: ActorRef, url: String, depth: Integer) extends Actor {
  receptionist ! CrawlRequest(url, depth)
  def receive = {
    case CrawlResponse(root, links) =>
      println(s"Root: $root")
      println(s"Links: ${links.toList.sortWith(_.length < _.length).mkString("\n")}")
      println("=========")
      println(s"Current Time ${System.currentTimeMillis}")
  }
}