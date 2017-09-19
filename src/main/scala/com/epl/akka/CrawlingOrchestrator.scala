package com.epl.akka

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Sink, Source }
import com.epl.akka.WebCrawler._

object CrawlingOrchestrator {
  def props() =
    Props[CrawlingOrchestrator]
}

class CrawlingOrchestrator() extends Actor {

  implicit val mat = ActorMaterializer()
  
  // this could be multiple ones
  val webCrawler = context.actorOf(Props[WebCrawler], "WebCrawler")
  val statefulFilter = context.actorOf(Props[], "WebCrawler")

  val crawlStream =
    Flow[CrawlRequest]
      .mapAsync(1){ req =>  }
      .to(Sink.actorRefWithAck(self, CrawlInit(), CrawlResponseAck(), CrawlComplete()))


  def receive = {
    case request: CrawlRequest =>
      // we materialize a "crawling stream" for each
      crawlStream.runWith(Source.single(request))
      
    case CrawlResponse(root, links) =>

      links.foreach { url =>
        context.actorOf(HTMLParser.props(url, 1))
      }
  }
}
