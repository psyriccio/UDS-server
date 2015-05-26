/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import akka.actor.ActorSystem
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.config.ConfigFactory
import java.io.File
import org.slf4j.LoggerFactory
import scala.io.StdIn

object UDSServer extends App {

  val lc = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext];
  StatusPrinter.print(lc);
  
  val conf = ConfigFactory.load(
    ConfigFactory.parseFile(
      new File("uds.conf")
    )
  )

  val akkaSystem = ActorSystem("uds-server", conf)
  val log = akkaSystem.log
  
  val host = if(args.length < 1) conf.getString("uds-server.host") else args(0)
  val port = if(args.length < 2) conf.getInt("uds-server.port") else args(1).toInt

  val httpUser = conf.getString("uds-server.user")
  val httpPass = conf.getString("uds-server.pass")

  log.info(s"${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}")

  StdIn.readLine
  akkaSystem.shutdown
  
}
