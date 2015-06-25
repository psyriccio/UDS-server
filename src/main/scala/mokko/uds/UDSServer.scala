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
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import org.slf4j.LoggerFactory
import scala.io.StdIn
import spray.can.Http

object UDSServer extends App with IServer with IServerManager with OSMXBeanImpl with RuntimeMXBeanImpl {

  def getHelo() = {
    s"${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}"
  }
  
  def getVersion = {
    s"${buildinfo.buildInfo.version}"
  }
  
  def resolvePluginName(sessionKey: String): String = {
    pluginsSessionKeys.get(sessionKey)
  }
  
  def isMessageAvailable(sessionKey: String): Boolean = {
    !pluginsMessageQueues.get(resolvePluginName(sessionKey)).isEmpty
  }
  
  def popMessage(sessionKey: String): Message = {
    pluginsMessageQueues.get(resolvePluginName(sessionKey)).poll
  }
  
  def popAllMessages(sessionKey: String): Array[Message] = {
    val result = pluginsMessageQueues.get(resolvePluginName(sessionKey)).toArray[Message](new Array[Message](0))
    pluginsMessageQueues.get(resolvePluginName(sessionKey)).clear()
    result
  }
  
  def sendMessage(sessionKey: String, message: Message) = {
    val copy = new Message(pluginsSessionKeys.get(sessionKey), message.getRecipient, message.getContent)
    log.info(s"sendMessage() sessionKey=${sessionKey}, from=${copy.getSender()}, to=${copy.getRecipient()}")
    if(!message.getSender().isEmpty) {
      pluginsMessageQueues.get(message.getRecipient()).put(copy)
    }
  }
  
  def log(msg: String) = {
    log.info(msg)
  }
  
  def resolveName(id: String) = {
    pluginsSessionKeys.get(id)
  }
  
  def getServerManager() = {
    this
  }
  
  def getPluginsDescriptors() = {
    var plugs: ArrayList[IPluginDescriptor] = new ArrayList[IPluginDescriptor]
    for(name: Object <- plugins.keySet.toArray) {
      val plugin = UDSServer.plugins.get(name)
      plugs.add(new PluginDescriptorImpl(plugin))
    }
    plugs
  }
  
  def loadPlugin(urlPrefix: String, config: String): Boolean = {
    log.info(s"loadPlugin(), urlPrefix=${urlPrefix}, config=${config}")
    val entryType = config.split(":")(0)
    val entryVal = config.split(":")(1)
    var processed = false
    if(entryType.compareToIgnoreCase("plugin") == 0) {
      log.info(s"Loading plugin ${urlPrefix} from path ${entryVal}")
      val pluginPathParts = entryVal.split("/")
      log.info(s"${pluginPathParts(0)} : ${pluginPathParts(1)}")
      val url = (new File(pluginsDir, pluginPathParts(0))).toURI.toURL
      val classLoader = new URLClassLoader(Array(url))
      pluginsClassLoaders.put(urlPrefix, classLoader)
      val pluginClass = classLoader.loadClass(pluginPathParts(1))
      val plugin: IServerPlugin = pluginClass.newInstance().asInstanceOf[IServerPlugin]
      plugins.put(urlPrefix, plugin)
      pluginsMessageQueues.put(urlPrefix, new LinkedBlockingQueue[Message]())
      val sessionKey = UUID.randomUUID.toString
      pluginsSessionKeys.put(sessionKey, urlPrefix)
      sessions.put(sessionKey, new ConcurrentHashMap[String, Object])
      plugin.init(this, sessionKey)
      log.info(s"Loaded plugin ${plugin.getName()} - ${plugin.getDescription()}")
      processed = true
    }
    if(entryType.compareToIgnoreCase("config") == 0) {
      log.info(s"Loading internal plugin (Config) ${urlPrefix}")
      val plugin = new mokko.uds.plugins.internal.Config()
      plugins.put(urlPrefix, plugin)
      pluginsMessageQueues.put(urlPrefix, new LinkedBlockingQueue[Message]())
      val sessionKey = UUID.randomUUID.toString
      pluginsSessionKeys.put(sessionKey, urlPrefix)
      sessions.put(sessionKey, new ConcurrentHashMap[String, Object])
      log.info(s"Loaded internal plugin (Config) ${plugin.getName()} - ${plugin.getDescription()}")
      plugin.init(this, sessionKey)
      processed = true
    }
    if(processed) {
      pluginsConfigs.put(urlPrefix, config)
    }
    processed
  }

  def unloadPlugin(urlPrefix: String) = {
    log.info(s"unloadPlugin(), prefix=${urlPrefix}")
    val plugin = plugins.get(urlPrefix)
    if(plugin != null) {
      log.info(s"plugin finded: ${plugin.getName()} - ${plugin.getDescription}")
      val classLoader = pluginsClassLoaders.get(urlPrefix).asInstanceOf[URLClassLoader]
      log.info(s"${classLoader.toString()} ${classLoader.getURLs.mkString}")
      pluginsClassLoaders.remove(urlPrefix)
      log.info("calling plugin.done()")
      plugin.done(this)
      plugins.remove(urlPrefix)
      log.info(s"deleting message queue ${pluginsMessageQueues.get(urlPrefix).toString()}")
      pluginsMessageQueues.remove(urlPrefix)
      for(sessionKey: Object <- pluginsSessionKeys.keySet.stream.sorted.toArray) {
        if(pluginsSessionKeys.get(sessionKey).equals(urlPrefix)) {
          log.info(s"deleting session key = ${sessionKey}")
          pluginsSessionKeys.remove(sessionKey)
        }
      }
      classLoader.close();
      log.info("class loader closed")
    } else {
      log.info("plugin not founded")
    }
  }
  
  def reloadPlugin(urlPrefix: String) = {
    val urlData: Array[Byte] = urlPrefix.getBytes("UTF-8").clone()
    val urlCopy = new Array[Byte](urlData.length)
    Array.copy(urlData, 0, urlCopy, 0, urlCopy.length)
    val urlPrefixCopy = new String(urlCopy, "UTF-8")
    val configData = pluginsConfigs.get(urlPrefixCopy).getBytes("UTF-8").clone()
    val configDataCopy = new Array[Byte](configData.length)
    Array.copy(configData, 0, configDataCopy, 0, configDataCopy.length)
    val configCopy = new String(configDataCopy, "UTF-8")
    log.info(s"copied urlPrefix=${urlPrefixCopy}")
    log.info(s"reloading plugin at ${urlPrefix}")
    unloadPlugin(urlPrefix)
    loadPlugin(urlPrefixCopy, configCopy)
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
  
  val plugins: ConcurrentHashMap[String, IServerPlugin] = new ConcurrentHashMap[String, IServerPlugin]()
  val pluginsConfigs: ConcurrentHashMap[String, String] = new ConcurrentHashMap[String, String]()
  val pluginsClassLoaders: ConcurrentHashMap[String, ClassLoader] = new ConcurrentHashMap[String, ClassLoader]()
  val pluginsMessageQueues: ConcurrentHashMap[String, LinkedBlockingQueue[Message]] = new ConcurrentHashMap()
  val pluginsSessionKeys: ConcurrentHashMap[String, String] = new ConcurrentHashMap[String, String]()
  val sessions: ConcurrentHashMap[String, ConcurrentHashMap[String, Object]] = new ConcurrentHashMap[String, ConcurrentHashMap[String, Object]]
  val clientSessions: ConcurrentHashMap[String, ClientSession] = new ConcurrentHashMap[String, ClientSession]()
  
  val treeConf = conf.getObject("uds-server.tree")
  for(key: Object <- treeConf.keySet.stream.sorted.toArray) {
    val value = conf.getString(s"uds-server.tree.${key}")
    if(!loadPlugin(key.asInstanceOf[String], value)) {
      log.error("Error processing tree entry " + key.asInstanceOf[String] + " ; skipped!")
    } else {
      pluginsConfigs.put(key.asInstanceOf[String], value)
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

class PluginDescriptorImpl(serverPlugin: IServerPlugin) extends IPluginDescriptor {
    
  def getName() = {
    serverPlugin.getName()
  }
  
  def getDescription() = {
    serverPlugin.getDescription()
  }
  
  def getURLPrefix() = {
    var result = ""
    for(name: Object <- UDSServer.plugins.keySet.toArray) {
      val plugin = UDSServer.plugins.get(name)
      if(plugin.equals(serverPlugin)) {
        result = name.asInstanceOf[String]
      }
    }
    result
  }
  
}