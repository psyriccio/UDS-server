/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.uds

import com.google.common.io.Files
import java.io.File
import spray.http._
import scala.concurrent.ExecutionContext
import spray.http.MediaTypes._
import spray.routing._

trait MainRoute extends Directives with AppLogging {
  
  implicit val executionContext = ExecutionContext.global

  val mainRoute: Route = {
    
    def path_(value: String) = {
      (pathPrefix(value) & pathEndOrSingleSlash)
    }
    
    def static(urlPath: String, fromPath: String) = {
      path(urlPath / Rest) { pathRest =>
        log.info(s"GET ${urlPath}/${pathRest}")
        def result(mediaType: MediaType) = {
          respondWithMediaType(mediaType) {
            complete {
              val source = Files.toByteArray(new File(s"${fromPath}/${pathRest}"))
              val entity = HttpEntity(HttpData(source))
              entity
            }
          }
        }
        val suff = pathRest.substring(pathRest.size-3)
        suff match {
          case "css" => result(`text/css`)
          case ".js" => result(`application/javascript`) 
          case _ => result(`application/octet-stream`)
        }
      }
    }
    
    def staticFile(urlPath: String, fileDir: String, fileName: String, mediaType: MediaType) = {
      path(urlPath) {
        log.info(s"GET ${urlPath}")
        respondWithMediaType(mediaType) {
          complete {
            val file = new File(new File(fileDir), fileName)
            if(file.exists()) {
              val result = HttpEntity(HttpData(Files.toByteArray(file)))
              file.delete()
              result  
            } else {
              StatusCodes.NotFound
            }
          }
        }
      }
    }
    
    def staticText(urlPath: String, text: String) = {
      path(urlPath) {
        log.info(s"GET ${urlPath}")
        respondWithMediaType(`text/plain`) {
          complete {
            HttpEntity(HttpData(text))
          }
        }
      }
    }
  
    path(Rest) { pathRest =>
      get {
        log.info(s"GET ${requestUri.toString}")
        val plugin = UDSServer.plugins.get(pathRest.split("/")(0))
        val result = if(plugin == null) new ServerResponce(StatusCode.NotFound, "404") else plugin.get(pathRest.replaceFirst(pathRest.split("/")(0), ""))
        val statusCode = StatusCodes.getForKey(result.getStatusCode).orNull
        respondWithMediaType(MediaType.custom(result.getMediaType)) {
          respondWithStatus(if(statusCode != null) statusCode else StatusCodes.InternalServerError) {
            complete {
              result.getData()
            }
          }
        }
      } 
    }
    
  }
  
}
