import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class MyFirstTest extends Simulation {

  //1 Http Conf

  val httpConf: HttpProtocolBuilder = http.baseUrl(url = "http://localhost:8080/app/")
    .header(name = "Accept", value = "application/json")
    //.proxy(Proxy("localhost", port=8888))

  // 2 Scenario Definition

  val scn: ScenarioBuilder = scenario(scenarioName = "My First Test")
    .exec(http(requestName = "Get All Games")
    .get("videogames"))

  // 3 Load Scenario

  setUp(
    scn.inject(atOnceUsers(users = 1))
  ).protocols(httpConf)
}
