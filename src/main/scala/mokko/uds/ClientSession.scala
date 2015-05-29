/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import java.util.concurrent.ConcurrentHashMap

class ClientSession(val sessionKey: String) extends ISession {

  val thisSession: ConcurrentHashMap[String, Object] = 
    if(UDSServer.sessions.contains(sessionKey)) 
      UDSServer.sessions.get(sessionKey) else UDSServer.sessions.put(sessionKey, new ConcurrentHashMap[String, Object])
  
  def getId(): String = {
    sessionKey
  }
  
  def clear() = {
    thisSession.clear()
  }
  
  def close() = {
    thisSession.clear
    UDSServer.sessions.remove(sessionKey)
    UDSServer.pluginsSessionKeys.remove(sessionKey)
  }

  def get(key: String): Object = {
    thisSession.get(key)
  }
  
  def put(key: String, obj: Object) = {
    thisSession.put(key, obj)
  }
  
}
