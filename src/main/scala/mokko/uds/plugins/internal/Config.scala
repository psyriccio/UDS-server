/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds.plugins.internal

import mokko.uds._

class Config extends IServerPlugin {

  def getName() = { "Config" }
  
  def getDescription = { "Config, internal plugin" }
 
  def get(uri: String) = {
    new ServerResponce(StatusCode.NotImplemented, "Not implemented")
  }
  
  def post(uri: String) = {
    new ServerResponce(StatusCode.NotImplemented, "Not implemented")
  }
  
}
