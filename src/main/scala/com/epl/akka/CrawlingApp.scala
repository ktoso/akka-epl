package com.epl.akka

import akka.actor.ActorSystem
import com.epl.akka.WebCrawler.CrawlRequest

object CrawlingApp extends App {
  val system = ActorSystem("CrawlerSystem")
  
  // we have one "main actor", it coordinates all tasks internally
  val main = system.actorOf(CrawlingOrchestrator.props(), "orchestrator")
  
  // start the crawling
  main ! CrawlRequest("https://www.premierleague.com/", 1)
}
