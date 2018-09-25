
name := "geolatte-geom-scala"

val geolatteGeomVersion = "1.4.0"

val commonResolvers = Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
)

val commonSettings = Seq(
  version := geolatteGeomVersion,
  scalaVersion := "2.12.6",
  resolvers ++= commonResolvers,
  scalacOptions in Test ++= Seq("-Yrangepos")
)


val commonDependencies = Seq(
  "org.geolatte" % "geolatte-geom" % geolatteGeomVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.specs2" %% "specs2-core" % "4.3.4" % "test"
)


lazy val geom = (project in file( "geom" )).settings(
  commonSettings,
  libraryDependencies ++= commonDependencies
)

lazy val playJson26 = (project in file ("play-json-26")).settings(
  commonSettings,
  libraryDependencies ++=
    commonDependencies ++ Seq( 
      "com.typesafe.play" %% "play-json" % "2.6.0",
     "org.geolatte" % "geolatte-geojson" % geolatteGeomVersion % "test"
    )
).dependsOn(geom)

lazy val root = (project in file( "." )).aggregate(
  geom,
  playJson26
)

