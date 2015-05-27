/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import akka.actor.ActorSystem
import akka.io.IO
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import scala.io.StdIn
import spray.can.Http

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

  val pluginsDir = new File(conf.getString("uds-server.pluginsPath"))
  
  val plugins: ConcurrentHashMap[String, IServerPlugin] = new ConcurrentHashMap();
  
  val pluginsConf = conf.getObject("uds-server.tree")
  for(key: Object <- pluginsConf.keySet.stream.sorted.toArray) {
    val pluginPath = conf.getString(s"uds-server.tree.${key}")
    log.info(s"Loading plugin ${key} from path ${pluginPath}")
    val pluginPathParts = pluginPath.split("/")
    log.info(s"${pluginPathParts(0)} : ${pluginPathParts(1)}")
    val url = (new File(pluginsDir, pluginPathParts(0))).toURI.toURL
    val classLoader = new URLClassLoader(Array(url))
    val pluginClass = classLoader.loadClass(pluginPathParts(1))
    val plugin: IServerPlugin = pluginClass.newInstance().asInstanceOf[IServerPlugin]
    plugins.put(key.toString, plugin)
    plugin.init()
    log.info(s"Loaded plugin ${plugin.getName()} - ${plugin.getDescription()}")
  }
    
  val host = if(args.length < 1) conf.getString("uds-server.host") else args(0)
  val port = if(args.length < 2) conf.getInt("uds-server.port") else args(1).toInt

  val httpUser = conf.getString("uds-server.user")
  val httpPass = conf.getString("uds-server.pass")

  log.info(s"${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}")

  val webService = akkaSystem.actorOf(WebServiceActor.props, "uds-server")
  IO(Http)(akkaSystem) ! Http.Bind(webService, host, port = port)
  
  StdIn.readLine
  akkaSystem.shutdown
  
}

trait AppLogging {
  val log = UDSServer.log
}