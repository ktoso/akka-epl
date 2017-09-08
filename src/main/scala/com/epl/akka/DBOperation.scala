package com.epl.akka


import java.util.Properties

import akka.actor.{Actor, ActorLogging}
import com.cloudant.client.api.{ClientBuilder, CloudantClient, Database}
import com.epl.akka.DBOperation.SaveToCloudantDatabase


object DBOperation{
  case class SaveToCloudantDatabase(jsonString: String)
}




/**
  * Created by sanjeevghimire on 9/5/17.
  */
class DBOperation(jsonString: String) extends Actor with ActorLogging{

  val dbName = "TeamTable"

  self ! SaveToCloudantDatabase(jsonString)


  override def receive: Receive = {

    case SaveToCloudantDatabase(jsonString: String) =>
      getCloudantClient().database(dbName,false)
        .save(jsonString)


  }

  /**
    * Get cloudant connection using connection parameters from config.properties
    */
  def getCloudantClient(): CloudantClient = {

    val (host, port, url, username, password, dbname, key, passcode) =
      try {
        val prop = new Properties()
        // prop.load(new FileInputStream("src/main/resources/config.properties"))

        prop.load(getClass().getResourceAsStream("/config.properties"))
        (
          prop.getProperty("host"),
          new Integer(prop.getProperty("port")),
          prop.getProperty("url"),
          prop.getProperty("username"),
          prop.getProperty("password"),
          prop.getProperty("dbname"),
          prop.getProperty("key"),
          prop.getProperty("passcode"))
      } catch {
        case e: Exception =>
          e.printStackTrace()
          sys.exit(1)
      }

    ClientBuilder.account(host)
      .username(username)
      .password(password)
      .build()
  }




}
