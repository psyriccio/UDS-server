/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds.plugins.internal

import mokko.uds._

class Config extends IServerPlugin {

  var server: IServer = null
  var sessionKey: String = null
  
  def getName() = { "Config" }
  
  def getDescription() = { "Config, internal plugin" }
 
  def init(srv: IServer, sessKey: String) = {
    server = srv
    sessionKey = sessKey
  }
  
  def done(srv: IServer) = {
    //
  }
  
  def get(uri: String, session: ISession, content: Array[Byte]) = {
    new ServerResponce(StatusCode.NotImplemented, s"Not implemented (${uri})")
  }
  
  def post(uri: String, session: ISession, content: Array[Byte]) = {
    new ServerResponce(StatusCode.NotImplemented, s"Not implemented (${uri})")
  }
  
}
