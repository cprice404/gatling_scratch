package com.puppetlabs.puppet.master.loadtest 
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._
import bootstrap._
import assertions._

class PuppetMasterLoadTest extends Simulation {

	val httpConf = httpConfig
			.baseURL("https://localhost:8140")
			.acceptHeader("pson, b64_zlib_yaml, yaml, raw")
			.acceptEncodingHeader("gzip;q=1.0,deflate;q=0.6,identity;q=0.3")
			.userAgentHeader("Ruby")


  // TODO: refactor, it's weird having these headers up here separated
  //  from their corresponding requests.

	val headers_1 = Map(
			"Accept" -> """s""",
			"Connection" -> """close"""
	)

	val headers_2 = Map(
			"Connection" -> """close"""
	)

	val headers_4 = Map(
			"Accept" -> """pson, b64_zlib_yaml, yaml, dot, raw""",
			"Connection" -> """close""",
			"Content-Type" -> """application/x-www-form-urlencoded"""
	)

	val headers_5 = Map(
			"Accept" -> """yaml, b64_zlib_yaml, raw""",
			"Connection" -> """close""",
			"Content-Type" -> """text/yaml"""
	)


	val scn = scenario("Scenario Name")
//		.exec(http("request_1")
//					.get("/production/certificate_revocation_list/ca")
//					.headers(headers_1)
//					.check(status.is(404))
//			)
//		.pause(106 milliseconds)
    .repeat(20) {
      exec(http("get node")
            .get("/production/node/localhost")
            .headers(headers_2)
        )
      .pause(124 milliseconds)
      .exec(http("get plugins")
            .get("/production/file_metadatas/plugins")
            .headers(headers_2)
            .queryParam("""checksum_type""", """md5""")
            .queryParam("""links""", """manage""")
            .queryParam("""recurse""", """true""")
            .queryParam("""ignore""", """---
    - ".svn"
    - CVS
    - ".git"""")
        )
      .pause(2)
      .exec(http("get catalog")
            .post("/production/catalog/localhost")
            .headers(headers_4)
              .param("""facts_format""", """b64_zlib_yaml""")
              .param("""facts""", """eNqlV2lz8jgS%2Fr6%2FwptPM8WAD4yvqqkaA%2BaIucFcX1KyLWwH35I5%2FOtXxkAC%0Aw7s7WyGhkPppPWpJ3a1WtVql%2Fp1m5pmOzE9oYWWSxTHEijKKbKgoHWBh9C%2BK%0ACkEAFcqPLOC7EcJEcgB%2BBpFCkSZFgdRyPUyGZylRA4Et8Bf5HqYh9BVq4IXZ%0A6SKxowB4oUK5yGZrUVqzosACCNdCiGsXhQBYwLZTiAj3G8MoHK9IliLbCmcp%0A9cbbRSeKYQqwFzrojDAMFKoNTQ%2BEF8xHpu0h7NnXWYdeiC%2FAjiwFpgeYIi8i%0ABryxNaHGiiXhLrGJCJ5iP0LeIar90joXpPYRpNBDQKFOkvBxXehNHpBd8x8R%0Asl3l7t3pL%2FLCQCtOPQuW3ZAYR0wstvQNYpf5w4%2F%2BOARkYrb8kUpLvfi6Ox%2BF%0AUrEMmauxglRjaqzEvD1t4U3pF%2FtIaAOA9jctrtGo3b43KpzdJ2owzLMNflQA%0AnEhmJ%2FM%2Fkl6wC1Xx90VXjhH4uvDMVi63QMViTTWZv3F%2BW9FdiaypQf4FxWIU%0A0maepr%2Fr%2FWJVX5O9WFe541%2BWXDb5tSnSC1OkF6ZI%2F8MU6bUpr0%2B4jKsAfH65%0Ac732gKXQhwDBEqgxVU6oOjCExN%2B%2Baz0MvxFcI8girnwNe%2BLt3xEbIiv1YlyO%0AvEQZVYQZxXKU3tYeWL7sYLk7QAx%2FjQUkgNIz8vJCKNYkluo2vyO7FJZL4qU7%0Ago4gvo6Qaw3%2BQX7Vl2v1%2BhMTjjDwnye5HtjrkyLgMUr3f4%2B7J%2FwhJp6wFx7%2B%0ASuPJ85iXWe%2B%2Be0cXwvxMI88u1dAOBJ5%2FfsiKMcAuIaUzlNKXLE67MI32GW2S%0AZEy7UQDpMhnRtfQQ0A4MEF3cClW2RjavGnOS8H%2Bo%2FuX4kUnmeD2CKHvwn9Jf%0ApF9Wo6f%2BvfsFXBpl9y5ziB8jhY5iTKNoh4tMTQfgAEPaBxgi%2FN8m%2Fy79m6A4%0AMzq%2BXJm3H8e7N%2F%2Bpenk1FerlSccuiQGyvDiNyIWAotSKshAXTnHFb%2FLCEfvk%0A5vB%2Fm%2F1OtaIU%2FrYY%2Fk55ItWaGNTtI3MMRf1FcTVB7PbyJwb2xwzcjxnqP2bg%0Af8zQ%2BDGD8GMG8ccMNz%2B5XkBxcX3EiKrC3VVw8bnvWZ%2B9ZZciHBGp4WwvVah%2F%0AHLS%2BZ15kdDH049IqUPZO%2Bb3eIqOuqRn6xaVBhDvgI3gVItdGYA9J4npTyadZ%0AH%2BWgxe6t%2BpD0Wk217xpoaDMNcX1SR%2Byi29i2DkNt%2Fd7ZT4fMZqvyWhiehtuJ%0A6YxEVTf3juQcELSsccNfe%2FSuH2XxQBrzKpuoljtYb%2BPJatuQWw7fk%2BeRe2SO%0Alb0z9jNp2jPrDSHutLzWxll0uMpn3OBSdrNd4qHT3Kybe01mF0ucDeRkkXL6%0ABuvjSlYPW01oqTsrnHTeC%2Fs709aiwjmBHjXM8LBdT0c511xMUVCBdm9KFPrq%0AIvKAlyyEhBUnqsAYoD3IgbNp2%2Fl8BSu5wNFynvd7y3lkLjpg3m1vnfaO7uzq%0AHX2X8RvOMHRrbfiMLXUAl5ntQz2eD8bh0h02xcl%2BJNNbPtiyNFcxRirqzzQj%0AQHmPpWNyX3QOg2jIsINzJIBggDs4Ewx%2FWO%2BeVH0u0wb3eU5a%2BCSk4sQYC6rv%0AXOyN8X7Tz8db5jQ9z8MZ3MfMajyTmlAL5lu%2FKdlW8u7q7baeaOFoN50i2al7%0AhoH1cLaMneFwYCaro5gAbpVvXXDEgTjfOcMlK%2B329imc6POM3H7QH61ozX0%2F%0A6LPzES0P0opeHYymrx4O645lrFxGW6wwm7Yq3eUcx9wknUXJzjpO43i1VDF9%0ABME6Pv75592r0hdedbY4rei21anaLMTqtG2F7lpfzruobQ0qZqLxXU7z0k3H%0A8%2BfHdoV3ZIzSxJzHc23vHrnZJlFdvxfTssYfz464c9ujzewgmHMJ2O%2BjlOes%0AxIdWD8zCzwC87%2FNRA0aVc25OWvQuQ9P2uofPp3oiDyOX7rcmUVcGq%2BUG4J7m%0AnntqZ%2BxtNgtHToO8r1vdbrZM9ATswk%2BWlyvz8z7xPwfi2BoaZ69lNMfS%2FqQ1%0AYrnnGstsyMu7QcdvOdJai1adykKGmWFX4hne00MrYKIBXofm2QJCAubskFuG%0AHt0adkWNbYBxb%2B5N3o0uaOq6FphCaA70mF2JVjdpTnVtHWaL3Exyiz143eUi%0A7zQ2E7ahM8GGP3aY5adnq3DGWoaXYEPjjImT3k8BWg%2FRrXHLz21vROJwFG0W%0AfWwGfm731PNosSng%2FmO%2F2Wz23BUyRwwaGB6XS3umE8FZ0pIkwUmTfKEzuRFO%0ArdyuO2dn2%2Btni0lj3m%2F6en3rRo7LbKEAErerZ8vBvqtzJiu%2BozAX0NVLsBfA%0APApJMTWZLy6SLPSSDBaPNFLdizuGvb0vsrhQLipHygZn9F34UQgK5EHoRlla%0AlvHMg5ykpii0C0SQRFbiS%2FDgpTgrKtNDUFQpH0XFgDAoau3yYUCeHTeV79mz%0AfMOjc0C9fRT0ZEwQvxWVLMPWqwxXZRmKZRSeV%2BosVWWk20PD8j0YYgum%2BPF5%0Af0Ne3BXkHeuloKz%2Bv7OzCluw13hB5CSumIQ8hv4DrD3sLw%3D%3D%0A""")
        )
      .pause(476 milliseconds)
      .exec(http("submit report")
            .put("/production/report/localhost")
            .headers(headers_5)
              .fileBody("report_body.txt")
        )
      .pause(100 milliseconds)
    }

	setUp(scn.users(10).ramp(10).protocolConfig(httpConf))
}