package de.az.demo.mn.opa

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._


/**
  * Gatling simulation for the Micronaut / OPA demo application
  */
class DemoSimulation extends Simulation {

  val appHost: String = getenv("app.host", "localhost")
  val appPort: String = getenv("app.port", 8080)

  val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtaWNyb25hdXQtb3BhLWRlbW8iLCJuYW1lIjoiTWljcm9uYXV0IE9QQSBEZW1vIiwiaWF0IjoxNTE2MjM5MDIyfQ.2sOzCwb9777B4yAP-nU5PQPFIjulRJxS9nKDNgHOvqA"

  val users = 100
  val rampDuration: FiniteDuration = 20 seconds
  val constantDuration: FiniteDuration = 40 seconds

  setUp(
    scenario("Load test")
      .randomSwitch(
        90.0 -> exec(http("/free")
          .get("/free")
          .check(status is 200)),
        8.0 -> exec(http("/protected (authorized)")
          .get("/protected")
          .header("Authorization", s"Bearer $token")
          .check(status is 200)),
        2.0 -> exec(http("/protected (unauthorized)")
          .get("/protected")
          .check(status is 401)))
      .inject(
        rampUsers(users) during rampDuration,
        constantUsersPerSec(users) during constantDuration))
    .protocols(
      http
        .baseUrl(s"http://$appHost:$appPort")
        .acceptHeader("text/plain"))
    .assertions(
      global.failedRequests.count is 0,
      global.responseTime.percentile3 lte 20,
      global.responseTime.percentile4 lte 100)

  def getenv[T](name:String, default: T): String = {
    System.getProperty(name, System.getenv(name.toUpperCase.replace('.', '_'))) match {
      case null => default.toString
      case x => x
    }
  }


}