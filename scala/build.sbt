val geolatteGeomVersion = "1.6.0"

val commonResolvers = Seq(
  Resolver.mavenLocal,
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
)

val publishSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ =>
    false
  },
  sonatypeProfileName := "org.geolatte",
  publishTo := sonatypePublishToBundle.value,
  pomExtra := pomInfo,
  credentials ++= publishingCredentials
)

lazy val disablePublishingRoot = Seq(
  Keys.publishLocal := {},
  Keys.publish := {},
  publish / skip := true
)

val commonSettings = Seq(
  organization := "org.geolatte",
  version := geolatteGeomVersion,
  scalaVersion := "2.13.2",
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  resolvers ++= commonResolvers,
  scalacOptions ++= Seq("-language:implicitConversions"),
  scalacOptions in Test ++= Seq("-Yrangepos", "--explain-types"),
  Test / publishArtifact := true
) ++ publishSettings

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
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    libraryDependencies ++= commonDependencies ++ Seq(
      "io.circe"                   %% "circe-generic"   % CirceVersion withJavadoc (),
      "org.specs2"                 %% "specs2-core"     % Specs2Version % "test" withJavadoc (),
      "org.geolatte"               % "geolatte-geojson" % geolatteGeomVersion % "test",
      "io.circe"                   %% "circe-parser"    % CirceVersion % "test" withJavadoc (),
      "org.scalacheck"             %% "scalacheck"      % scalaCheckVersion % "test",
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test"
    )
  )
  .dependsOn(geom)

lazy val slick = (project in file("slick"))
  .settings(
    commonSettings,
    name := "geolatte-geom-slick",
    libraryDependencies ++= commonDependencies ++ Seq(
      "com.github.tminglei" %% "slick-pg" % "0.19.0",
      "com.typesafe.slick"  %% "slick"    % "3.3.2"
    )
  )
  .dependsOn(geom)

lazy val root = (project in file(".")).settings(
  commonSettings ++ disablePublishingRoot, 
  crossScalaVersions := Nil
  ).aggregate(
    geom,
    playJson28,
    circeGeoJson,
    slick
  )

lazy val pomInfo = <url>https://github.com/geolatte/geolatte-geom</url>
  <licenses>
    <license>
      <name>LGPL</name>
      <url>https://www.gnu.org/licenses/lgpl-3.0.en.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
    <scm>
    <url>git@github.com:geolatte/geolatte-geom.git</url>
    <connection>scm:git@github.com:geolatte/geolatte-geom.git</connection>
  </scm>
  <developers>
    <developer>
      <id>Geolatte</id>
      <name>Geolatte group</name>
      <url>http://github.com/geolatte</url>
    </developer>
  </developers>
  
val publishingCredentials = (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield
  Seq(
    Credentials("Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      username,
      password))).getOrElse(Seq())

