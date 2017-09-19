package com.epl.akka

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, ReceiveTimeout }
import com.epl.akka.HTMLParser.Done
import com.epl.akka.VisitedURLFilter.{ Result, ValidateUrl }


object VisitedURLFilter {

  final case class ValidateUrl(url: String, depth: Integer)

  final case class Result(url: String, links: Set[String])

  // avoid names like "result", they dont tell anything about the intention behind it

  val filter = () => (url: ValidateUrl) => {
    
  }
  
}


// avoid the "pass in stuff as constructor and then just send it to self" pattern you seem to use in a few places
// it does not make much sense to conflate actor creation with such message send. The creation of an actor should
// only mean "ok now it's ready to do things", and then you send it a message - which and it'll react to that (reactive, remember)
class VisitedURLFilter {

  var visitedUrl = Set.empty[String]
  var childUrls = Set.empty[ActorRef]

  import scala.concurrent.duration._

  self ! ValidateUrl(rootUrl, depth)
  context.setReceiveTimeout(10 seconds)


  override def receive: Receive = {
    case ValidateUrl(rootUrl, depth) =>
      if (!visitedUrl(rootUrl) && depth > 0)
        childUrls += context.actorOf(Props[HTMLParser](new HTMLParser(rootUrl, depth - 1)))
      visitedUrl += rootUrl

    case Done =>
      childUrls -= sender
      visitedUrl -= rootUrl
      if (childUrls.isEmpty) context.parent ! Result(rootUrl, visitedUrl)

    case ReceiveTimeout => 
      childUrls foreach (_ ! HTMLParser.Abort)

  }

}
