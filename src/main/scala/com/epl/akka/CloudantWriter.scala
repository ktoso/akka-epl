package com.epl.akka


import akka.actor.{ Actor, ActorLogging }
import com.cloudant.client.api.{ ClientBuilder, CloudantClient }
import com.epl.akka.CloudantWriter.SaveToCloudantDatabase


object CloudantWriter {

  final case class SaveToCloudantDatabase(jsonString: String)

}


class CloudantWriter(jsonString: String) extends Actor with ActorLogging {
  private val config = context.system.settings.config
  val dbname = config.getString("dbname") 

  val client: CloudantClient = {
    val host = config.getString("host")
    val port = config.getInt("port") // not used
    val url = config.getString("url") // not used
    val username = config.getString("username")
    val password = config.getString("password")
    val key = config.getString("key") // not used
    val passcode = config.getString("passcode") // not used

    ClientBuilder.account(host)
      .username(username)
      .password(password)
      .build()
  }

  self ! SaveToCloudantDatabase(jsonString)
  override def receive: Receive = {
    case SaveToCloudantDatabase(jsonString: String) =>
      client
        .database(dbname, false)
        .save(jsonString)


  }



}
