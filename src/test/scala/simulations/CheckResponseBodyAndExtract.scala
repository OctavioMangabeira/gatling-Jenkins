package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class CheckResponseBodyAndExtract extends Simulation{

  val httpConf = http.baseUrl(url = "http://localhost:8080/app/")
    .header(name = "Accept", value = "application/json")

  val scn = scenario("Check JSON Path")

    .exec(http("Get specific game")
      .get("videogames/1")
      .check(jsonPath(path = "$.name").is(expected = "Resident Evil 4")))

    .exec(http(requestName = "Get all video games")
    .get("videogames")
    .check(jsonPath(path = "$[1].id").saveAs(key = "gameId")))
    .exec { session => println(session); session}

    .exec(http(requestName = "Get specific game")
    .get("videogames/${gameId}")
    .check(jsonPath(path = "$.name")is(expected = "Gran Turismo 3"))
    .check(bodyString.saveAs(key = "responseBody")))
    .exec {session => println(session("responseBody").as[String]); session }

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
