package com.epl.akka

import java.net.URL

import akka.actor.{Actor, ActorLogging, Status}
import com.epl.akka.HTMLParser._
import com.epl.akka.URLValidator.ValidateUrl
import org.jsoup.Jsoup
import play.api.libs.json.Json

import scala.util.{Failure, Success}


object HTMLParser {

  case class Done() {}

  case class Abort() {}

  case class GetFixturesAndConvertToJson(fixturesBody: String) {}

  case class GetResultsAndConvertToJson(resultsBody: String) {}

  case class GetTablesAndConvertToJson(tablesBody: String) {}

}


/**
  * Created by sanjeevghimire on 9/1/17.
  */
class HTMLParser(url: String, depth: Int) extends Actor with ActorLogging {

  implicit val ec = context.dispatcher
  import scala.collection.JavaConverters._

  val currentHost = new URL(url).getHost

  val isFixtures = url.endsWith("fixtures")
  val isTables = url.endsWith("tables")
  val isResults = url.endsWith("results")

  println("is Table:: "+isTables)

  WebHttpClient.get(url) onComplete {
    case Success(body) if isResults => self ! GetResultsAndConvertToJson(body)
    case Success(body) if isFixtures => self ! GetFixturesAndConvertToJson(body)
    case Success(body) if isTables => self ! GetTablesAndConvertToJson(body)
    case Success(body) => self ! body
    case Failure(err) => self ! Status.Failure(err)
  }

  def getValidLinks(content: String): Iterator[String] = {
    Jsoup.parse(content, this.url).select("a[href]").iterator().asScala.map(_.absUrl("href"))
  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }

  def getFixturesAndConvertToJson(fixturesBody: String): String ={
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

  def getResultsAndConvertToJson(resultsBody: String): String ={
    return null
  }

  override def receive = {
    case body: String =>
      getValidLinks(body)
        .filter(link => link != null && link.length > 0)
        .filter(link => !link.contains("mailto"))
        .filter(link => !link.equals(url))
        .filter(link =>  currentHost  == new URL(link).getHost)
        //.filter(link => (link.endsWith("fixtures") || link.endsWith("tables") || link.endsWith("results")))
        .filter(link => (link.endsWith("tables")))
        .foreach(context.parent ! ValidateUrl(_, depth))

      stop()

    case GetTablesAndConvertToJson(tablesBody: String) =>
      val jsonStr = getTablesAndConvertToJson(tablesBody)
      // val dbActor = context.actorOf(Props[DBOperation](new DBOperation(jsonStr)))
      // dbActor ! getResultsAndConvertToJson(tablesBody)

      stop()

    case GetFixturesAndConvertToJson(fixturesBody: String) =>
      getFixturesAndConvertToJson(fixturesBody)
      stop()

    case GetResultsAndConvertToJson(resultsBody: String) =>
      getResultsAndConvertToJson(resultsBody)
      stop()


    case _: Status.Failure => stop()

    case Abort => stop()

  }
}
