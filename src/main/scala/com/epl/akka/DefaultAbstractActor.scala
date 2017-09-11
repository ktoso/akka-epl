package com.epl.akka

import akka.actor.AbstractLoggingActor
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import scala.concurrent.duration.Duration



abstract class DefaultAbstractActor extends AbstractLoggingActor {

  override def supervisorStrategy(): SupervisorStrategy =
    new OneForOneStrategy(-1, Duration.Inf, t => {
      log.error(t, "Error processing actor's message:")
      SupervisorStrategy.resume
    })

}