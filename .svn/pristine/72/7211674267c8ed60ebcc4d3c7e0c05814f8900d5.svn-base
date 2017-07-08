name := "fincashApi"
version := "1.0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "springio-release" at "http://repo.spring.io/release/"

// unmanagedBase := baseDirectory.value / "live_lib"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.1",
  "org.reactivemongo" %% "reactivemongo-play-json" % "0.12.1",
  "com.typesafe.play" %% "play-slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.1.0",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "mysql" % "mysql-connector-java" % "5.1.23",
  "com.pauldijou" % "jwt-play-json_2.11" % "0.12.0",
  "org.apache.solr" % "solr-solrj" % "6.2.0",
  "org.springframework.ws" % "spring-ws-core" % "2.4.0.RELEASE",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "com.typesafe" % "config" % "1.3.1",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "nu.validator.htmlparser" % "htmlparser" % "1.4",
  "org.htmlparser" % "htmlparser" % "2.1",
  "org.jsoup" % "jsoup" % "1.10.2",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.7",
  "com.twilio.sdk" % "twilio" % "7.4.0",
  "org.apache.poi" % "poi" % "3.15",
  "com.amazonaws" % "aws-java-sdk" % "1.3.11",
  "com.nulab-inc" %% "play2-oauth2-provider" % "1.2.0",
  cache,
  ws,
  specs2 % Test,
  filters
)
//twirl template engine common imports
TwirlKeys.templateImports ++= Seq("utils.mail._", "models._")

