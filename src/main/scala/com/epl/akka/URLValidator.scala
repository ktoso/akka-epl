package com.epl.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import com.epl.akka.HTMLParser.Done
import com.epl.akka.URLValidator.{Result, ValidateUrl}


object URLValidator {

  case class ValidateUrl(url: String, depth: Integer) {}

  case class Result(url: String, links: Set[String]) {}

}



/**
  * Created by sanjeevghimire on 9/1/17.
  */
class URLValidator(rootUrl: String, depth: Integer) extends Actor with ActorLogging{


  var visitedUrl = Set.empty[String]
  var childUrls = Set.empty[ActorRef]

  import scala.concurrent.duration._

  self ! ValidateUrl(rootUrl, depth)
  context.setReceiveTimeout(10 seconds)


  override def receive: Receive = {
    case ValidateUrl(rootUrl,depth) =>
      if(!visitedUrl(rootUrl) && depth > 0)
        childUrls += context.actorOf(Props[HTMLParser](new HTMLParser(rootUrl,depth - 1)))
      visitedUrl += rootUrl

    case Done =>
      childUrls -= sender
      visitedUrl -= rootUrl
      if (childUrls.isEmpty) context.parent ! Result(rootUrl, visitedUrl)

    case ReceiveTimeout => childUrls foreach (_ ! HTMLParser.Abort)

  }

}
