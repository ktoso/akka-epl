package com.epl.akka

import java.net.URL

import akka.actor.{Actor, ActorLogging, Props, Status}
import com.epl.akka.HTMLParser.{Abort, Done}
import com.epl.akka.URLValidator.ValidateUrl
import org.jsoup.Jsoup
import play.api.libs.json.{JsObject, Json}

import scala.util.{Failure, Success}


object HTMLParser {

  case class Done() {}

  case class Abort() {}

  case class GetFixturesAndConvertToJson() {}

  case class GetResultsAndConvertToJson() {}

  case class GetTablesAndConvertToJson(tablesBody: String) {}

}


/**
  * Created by sanjeevghimire on 9/1/17.
  */
class HTMLParser(url: String, depth: Int) extends Actor with ActorLogging{

  implicit val ec = context.dispatcher
  import scala.collection.JavaConverters._

  val currentHost = new URL(url).getHost
  val isFixtures = url.endsWith("fixtures")
  val isTables = url.endsWith("tables")
  val isResults = url.endsWith("results")

  WebHttpClient.get(url) onComplete {
    case Success(fixturesBody) if isFixtures => self ! fixturesBody
    case Success(resultsBody) if isResults => self ! resultsBody
    case Success(tablesBody) if isTables => self ! tablesBody
    case Success(body) => self ! body
    case Failure(err) => self ! Status.Failure(err)
  }

  def getValidLinks(content: String): Iterator[String] = {
    Jsoup.parse(content, this.url).select("a[href]").iterator().asScala.map(_.absUrl("href"))
  }

  def receive = {
    case body: String =>
      getValidLinks(body)
        .filter(link => link != null && link.length > 0)
        .filter(link => !link.contains("mailto"))
        .filter(link =>  currentHost  == new URL(link).getHost)
        .filter(link => !(currentHost == link))
        .filter(link => isFixtures || isResults || isTables)
        .foreach(context.parent ! ValidateUrl(_, depth))

      stop()

    case resultsBody: String =>
      val jsonStr = getTablesAndConvertToJson(resultsBody)
      val dbActor = context.actorOf(Props[DBOperation](new DBOperation(jsonStr)))
      dbActor ! getResultsAndConvertToJson(resultsBody)

      stop()

    case fixturesBody: String =>
      getFixturesAndConvertToJson(fixturesBody)
      stop()

    case tablesBody: String =>
      getTablesAndConvertToJson(tablesBody)
      stop()


    case _: Status.Failure => stop()

    case Abort => stop()

  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }

  def getFixturesAndConvertToJson(tablesBody: String): String ={
    return null
  }

  def getTablesAndConvertToJson(tablesBody: String): String ={
    // name of the array in the JSON
    val arrayName:String = "teams"

    val headings: List[String] = List("Position","teamLongName","teamShortName","Played","Won", "Drawn", "Lost","GF","GA","GD", "Points");

    var i = 0

    val plTable = scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, String]]()

    Jsoup.parse(tablesBody, this.url)
      .select("tbody.tableBodyContainer").first()
      .select("tr").asScala
        .foreach(trElement => {
          val teamName = trElement.attr("data-filtered-table-row-name")
          val teamStats = scala.collection.mutable.Map[String, String]()
          plTable += teamName -> teamStats
          trElement.select("td").asScala
            .foreach(tdElement => {
              if (!tdElement.is("revealMore") && !tdElement.is("expandable") &&  !tdElement.is("form hideMed")) {
                if (tdElement.is("value")) {
                  teamStats+=(headings(i) -> tdElement.text())
                } else if (tdElement.is("team")) {
                  i+=1
                  teamStats+=(headings(i) -> tdElement.select("a").select("span.long").text())
                  i+=1
                  teamStats+= (headings(i) -> tdElement.select("a").select("span.short").text())
                }else{
                  i+=1
                  teamStats+=(headings(i) -> tdElement.text())
                }
              }
            }
            )
          }
        )


    val jsonString:String = Json.stringify(Json.toJson(plTable))

    println(jsonString)


    return jsonString
  }

  def getResultsAndConvertToJson(tablesBody: String): String ={
    return null
  }



}
