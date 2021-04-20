package finalSimulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class VideoGameFullTestTemplate extends Simulation {

  /* Helper methods */

  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getRandomDate(startDate: LocalDate, random: Random): String = {
    startDate.minusDays(random.nextInt(30)).format(pattern)
  }

 //runtime variables
  def UserCount: Int = getProperty(propertyName = "USERS", defaultValue = "3").toInt
  def rampDuration: Int = getProperty(propertyName = "RAMP_DURATION", defaultValue = "10").toInt
  def testDuration: Int = getProperty(propertyName = "DURATION", defaultValue = "60").toInt

  // other variables
  var idNumbers = (20 to 1000).iterator
  val rnd = new Random()
  val now = LocalDate.now()
  val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  //** Custom Feeder */
  val customFeeder = Iterator.continually(Map(
    "gameId" -> idNumbers.next(),
    "name" -> ("Game-" + randomString(length = 5)),
    "releaseDate" -> getRandomDate(now, rnd),
    "reviewScore" -> rnd.nextInt(100),
    "category" -> ("Category-" + randomString(length = 6)),
    "rating" -> ("Rating-" + randomString(length = 4))
  ))

  // HTTP CONFIG
  val httpConf: HttpProtocolBuilder = http.baseUrl(url = "http://localhost:8080/app/")
    .header(name = "Accept", value = "application/json")

  //BEFORE

  before {
    println(s"Running test with ${UserCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  // HTTP CALLS
  def getAllVideoGames() = {
    exec(
      http(requestName = "Get all video games")
        .get("videogames")
        .check(status.is(200))
    )
  }

  def createNewGame() = {
      feed(customFeeder)
        .exec(
          http("Create New Game")
            .post(url = "videogames")
            .body(ElFileBody(filePath = "bodies/NewGameTemplate.json")).asJson
            .check(status.is(200)))
  }

  def getSingleGame() = {
    exec(
      http(requestName = "Get Single Game that just have created")
        .get(url = "videogames/${gameId}")
        .check(jsonPath("$.name").is("${name}"))
        .check(status.is(200))
    )
  }

  def deleteGameCreated() = {
    exec(
      http(requestName = "Delete a Game that just have created")
        .delete(url = "videogames/${gameId}")
        .check(status.is(200))
    )
  }


  // SCENARIO DESIGN

  val scn = scenario(scenarioName = "Video Game DB")
    .forever() {
      exec(getAllVideoGames())
        .pause(2)
        .exec(createNewGame())
        .pause(2)
        .exec(getSingleGame())
        .pause(2)
        .exec(deleteGameCreated())
    }

  // SETUP LOAD SIMULATION

  setUp(
    scn.inject(
      nothingFor(5 seconds),
      rampUsers(UserCount) during (rampDuration seconds))
  ).protocols(httpConf)
      .maxDuration(testDuration.seconds)

  //AFTER

  after {
    println("Stress test completed")
  }

}
