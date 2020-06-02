val geolatteGeomVersion = "1.5.0-SNAPSHOT"

val commonResolvers = Seq(
  Resolver.mavenLocal,
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
)

val commonSettings = Seq(
  organization := "org.geolatte",
  version := geolatteGeomVersion,
  scalaVersion := "2.13.2",
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  resolvers ++= commonResolvers,
  scalacOptions ++= Seq("-language:implicitConversions"),
  scalacOptions in Test ++= Seq("-Yrangepos", "--explain-types")
)

val Specs2Version     = "4.9.4"
val CirceVersion      = "0.13.0"
val jacksonVersion    = "2.11.0"
val scalaCheckVersion = "1.14.3"
val scalaTestVersion  = "3.1.2"

val commonDependencies = Seq(
  "org.geolatte"  % "geolatte-geom" % geolatteGeomVersion,
  "org.scalatest" %% "scalatest"    % scalaTestVersion % "test",
  "org.specs2"    %% "specs2-core"  % Specs2Version % "test"
)

lazy val geom = (project in file("geom")).settings(
  commonSettings,
  name := "geolatte-geom-scala",
  libraryDependencies ++= commonDependencies
)

lazy val playJson28 = (project in file("play-json-28"))
  .settings(
    commonSettings,
    name := "geolatte-geom-playjson28",
    libraryDependencies ++=
      commonDependencies ++ Seq(
        "com.typesafe.play" %% "play-json"       % "2.8.1",
        "org.geolatte"      % "geolatte-geojson" % geolatteGeomVersion % "test"
      )
  )
  .dependsOn(geom)

lazy val circeGeoJson = (project in file("circe-geojson"))
  .settings(
    commonSettings,
    name := "geolatte-geom-circe",
    libraryDependencies ++= commonDependencies ++ Seq(
      "io.circe"                   %% "circe-generic"   % CirceVersion withJavadoc (),
      "org.specs2"                 %% "specs2-core"     % Specs2Version % "test" withJavadoc (),
      "org.geolatte"               % "geolatte-geojson" % geolatteGeomVersion % "test",
      "org.scalacheck"             %% "scalacheck"      % scalaCheckVersion % "test",
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test"
    )
  )
  .dependsOn(geom)

lazy val root = (project in file(".")).aggregate(
  geom,
  playJson28,
  circeGeoJson
)
