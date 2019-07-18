
val geolatteGeomVersion = "1.5.0-SNAPSHOT"

val commonResolvers = Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
)

val commonSettings = Seq(
  organization := "org.geolatte",
  version := geolatteGeomVersion,
  scalaVersion := "2.12.8",
  resolvers ++= commonResolvers,
  scalacOptions in Test ++= Seq("-Yrangepos")
)

val Specs2Version      = "4.5.1"
val CirceVersion       = "0.11.1"
val jacksonVersion     = "2.9.9"

val commonDependencies = Seq(
  "org.geolatte" % "geolatte-geom" % geolatteGeomVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.specs2" %% "specs2-core" % Specs2Version % "test"
)


lazy val geom = (project in file( "geom" )).settings(
  commonSettings,
  name := "geolatte-geom-scala",
  libraryDependencies ++= commonDependencies
)

lazy val playJson26 = (project in file ("play-json-26")).settings(
  commonSettings,
  name := "geolatte-geom-playjson26",
  libraryDependencies ++=
    commonDependencies ++ Seq( 
      "com.typesafe.play" %% "play-json" % "2.6.0",
      "org.geolatte" % "geolatte-geojson" % geolatteGeomVersion % "test"
    )
).dependsOn(geom)


lazy val circeGeoJson = (project in file ("circe-geojson")).settings(
  commonSettings,
  name := "geolatte-geom-circe",
  libraryDependencies ++= commonDependencies ++ Seq(
    "io.circe"       %% "circe-generic"       % CirceVersion withJavadoc (),
    "org.specs2"     %% "specs2-core"         % Specs2Version % "test" withJavadoc (),
    "org.geolatte" % "geolatte-geojson" % geolatteGeomVersion % "test",
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test"
    )
).dependsOn(geom)


lazy val root = (project in file( "." )).aggregate(
  geom,
  playJson26,
  circeGeoJson
)

