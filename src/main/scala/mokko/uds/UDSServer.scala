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
import com.typesafe.config.ConfigFactory
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import scala.io.StdIn
import spray.can.Http

object UDSServer extends App with IServer {

  def getHelo() = {
    s"${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}"
  }
  
  def getVersion = {
    s"${buildinfo.buildInfo.version}"
  }
  
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
  
  val treeConf = conf.getObject("uds-server.tree")
  for(key: Object <- treeConf.keySet.stream.sorted.toArray) {
    val value = conf.getString(s"uds-server.tree.${key}")
    val entryType = value.split(":")(0)
    val entryVal = value.split(":")(1)
    var processed = false
    if(entryType.compareToIgnoreCase("plugin") == 0) {
      log.info(s"Loading plugin ${key} from path ${entryVal}")
      val pluginPathParts = entryVal.split("/")
      log.info(s"${pluginPathParts(0)} : ${pluginPathParts(1)}")
      val url = (new File(pluginsDir, pluginPathParts(0))).toURI.toURL
      val classLoader = new URLClassLoader(Array(url))
      val pluginClass = classLoader.loadClass(pluginPathParts(1))
      val plugin: IServerPlugin = pluginClass.newInstance().asInstanceOf[IServerPlugin]
      plugins.put(key.asInstanceOf[String], plugin)
      plugin.init(this)
      log.info(s"Loaded plugin ${plugin.getName()} - ${plugin.getDescription()}")
      processed = true
    }
    if(entryType.compareToIgnoreCase("config") == 0) {
      log.info(s"Loading internal plugin (Config) ${key}")
      val plugin = new mokko.uds.plugins.internal.Config()
      plugins.put(key.asInstanceOf[String], plugin)
      log.info(s"Loaded internal plugin (Config) ${plugin.getName()} - ${plugin.getDescription()}")
      processed = true
    }
    if(!processed) {
      log.error("Error processing tree entry " + key.asInstanceOf[String] + " ; skipped!")
    }
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