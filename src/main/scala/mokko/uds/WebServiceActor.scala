/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import akka.actor.Props
import spray.routing.HttpServiceActor
import spray.routing.Route

class WebServiceActor(route: Route) extends HttpServiceActor {
  def receive: Receive = runRoute(route)
}

object WebServiceActor extends MainRoute {
  val route = mainRoute
  val props = Props(new WebServiceActor(route))
}
