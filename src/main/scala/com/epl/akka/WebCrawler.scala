package com.epl.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.epl.akka.VisitedURLFilter.Result
import com.epl.akka.WebCrawler.{CrawlRequest, CrawlResponse}


object WebCrawler {
  final case class CrawlRequest(url: String, depth: Integer)
  final case class CrawlInit()
  final case class CrawlComplete()
  
  final case class CrawlResponse(url: String, links: Set[String])
  final case class CrawlResponseAck()
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
        requestedCrawlActor = context.actorOf(Props[VisitedURLFilter](new VisitedURLFilter(url, depth)))
      }
      client = sender

    case Result(url, links) =>
      context.stop(requestedCrawlActor)
      client ! CrawlResponse(url, links)
  }

}
