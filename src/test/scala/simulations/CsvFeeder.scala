package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class CsvFeeder extends Simulation{

  val httpConf = http.baseUrl(url = "http://localhost:8080/app/")
    .header(name = "Accept", value = "application/json")

  val csvFeeder = csv("data/gameCsvFile.csv").circular

  def getSpecificVideoGame(): ChainBuilder = {
    repeat(10) {
      feed(csvFeeder)
        .exec(http(requestName = "Get specific video game")
        .get("videogames/${gameId}")
        .check(jsonPath(path = "$.name").is(expected = "${gameName}"))
        .check(status.is(expected = 200)))
        .pause(1)
    }
  }

  val scn = scenario(scenarioName = "Csv Feeder test")
    .exec(getSpecificVideoGame())

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)

}
