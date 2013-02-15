package com.puppetlabs.http.server

import akka.util.{Duration, Timeout}
import akka.actor._
import spray.util.{ConfigUtils, SprayActorLogging}
import spray.http.{HttpResponse, HttpRequest}
import spray.can.server.{ServerSettings, SprayCanHttpServerApp}
import javax.net.ssl.{TrustManagerFactory, KeyManagerFactory, SSLContext}
import java.security.{SecureRandom, KeyStore}
import spray.io.ServerSSLEngineProvider
import com.typesafe.config.ConfigFactory


class HelloWorldService extends Actor with SprayActorLogging {
  implicit val timeout: Timeout = Duration(1, "sec") // for the actor 'asks' we use below

  def receive = {
    case _: HttpRequest => sender ! HttpResponse(status = 200, entity = "Hello World!")
  }
}

object SprayHelloWorld extends App with SprayCanHttpServerApp { //with MySslConfiguration {

  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
  // since we want non-default settings in this example we make a custom SSLContext available here
  implicit def sslContext: SSLContext = {
    val keyStoreResource = "/com/puppetlabs/http/server/jetty.keystore"
    val password = "puppet"

    val keyStoreResourceStream = getClass.getResourceAsStream(keyStoreResource)

    println("KeyStore resource: '" + keyStoreResourceStream + "'")

    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(keyStoreResourceStream, password.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  // if there is no ServerSSLEngineProvider in scope implicitly the HttpServer uses the default one,
  // since we want to explicitly enable cipher suites and protocols we make a custom ServerSSLEngineProvider
  // available here
  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_128_CBC_SHA"))
//      engine.setEnabledCipherSuites(engine.getSupportedCipherSuites)
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
      engine
    }
  }


  val customConf = ConfigFactory.parseString(
    """
  # check the reference.conf in spray-can/src/main/resources for all defined settings
  spray.can.server {
    # uncomment the next line for making this an HTTPS example
    ssl-encryption = on
  }
""")

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[HelloWorldService])

  // create a new HttpServer using our handler and tell it where to bind to
  newHttpServer(handler = handler,
    settings = new ServerSettings(ConfigFactory.load(customConf))) !
    Bind(interface = "localhost", port = 8140)

}