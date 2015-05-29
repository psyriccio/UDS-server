/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import java.util.concurrent.ConcurrentHashMap

class ClientSession(val sessionKey: String) extends ISession {

  val thisSession: ConcurrentHashMap[String, Object] = UDSServer.sessions.get(sessionKey)
  
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
