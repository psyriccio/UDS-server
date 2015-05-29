/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds.plugins.internal

import mokko.uds._

class Config extends IServerPlugin {

  var server: IServer = null
  
  def getName() = { "Config" }
  
  def getDescription() = { "Config, internal plugin" }
 
  def init(srv: IServer) = {
    server = srv
  }
  
  def done(srv: IServer) = {
    //
  }
  
  def get(uri: String, session: ISession) = {
    new ServerResponce(StatusCode.NotImplemented, s"Not implemented (${uri})")
  }
  
  def post(uri: String, session: ISession) = {
    new ServerResponce(StatusCode.NotImplemented, s"Not implemented (${uri})")
  }
  
}
