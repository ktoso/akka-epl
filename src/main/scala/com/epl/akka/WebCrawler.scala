package com.epl.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.epl.akka.URLValidator.Result
import com.epl.akka.WebCrawler.{CrawlRequest, CrawlResponse}


object WebCrawler {
  case class CrawlRequest(url: String, depth: Integer) {}
  case class CrawlResponse(url: String, links: Set[String]) {}
}



/**
  * Created by sanjeevghimire on 8/30/17.
  */
class WebCrawler extends Actor with ActorLogging{

  var requestedCrawlActor: ActorRef = null
  var client: ActorRef = null


  override def receive: Receive = {
    case CrawlRequest(url, depth) =>
      if (requestedCrawlActor == null) {
        requestedCrawlActor = context.actorOf(Props[URLValidator](new URLValidator(url, depth)))
      }
      client = sender

    case Result(url, links) =>
      context.stop(requestedCrawlActor)
      client ! CrawlResponse(url, links)
  }

}
