package simulations

import com.fasterxml.jackson.databind.PropertyName
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps

class RuntimeParameters extends Simulation {

  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def userCount: Int = getProperty(propertyName = "USERS", defaultValue = "5").toInt
  def rampDuration: Int = getProperty(propertyName = "RAMP_DURATION", defaultValue = "10").toInt
  def testDuration: Int = getProperty(propertyName = "DURATION", defaultValue = "60").toInt

  before{
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total test duration: ${testDuration} seconds")
  }

  val httpConf = http.baseUrl(url = "http://localhost:8080/app/")
    .header(name = "Accept", value = "application/json")

  def getAllVideoGames() = {
    exec(
      http("Get all video games")
        .get("videogames")
        .check(status.is(200))
    )
  }

  val scn = scenario("Get all video games")
    .forever(
      exec(getAllVideoGames())
    )

  setUp(
    scn.inject(
      nothingFor(5 seconds),
      rampUsers (userCount) during(rampDuration second)
    )
  ).protocols(httpConf)
    .maxDuration(testDuration seconds)

}
