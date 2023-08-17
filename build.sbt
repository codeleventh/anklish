lazy val root = (project in file("."))
  .settings(
    organization := "ru.eleventh",
    name := "anklish",
    version := "1.1",
    scalaVersion := "2.13.9",
    scalacOptions ++= Seq("-Ymacro-annotations", "-deprecation"),
    libraryDependencies ++= Seq(
      "org.typelevel" % "cats-effect_2.13" % CatsVersion,
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.github.scopt" % "scopt_2.13" % ScoptVersion,
      "io.circe"        %% "circe-generic"        % CirceVersion,
      "io.circe"        %% "circe-generic-extras" % CirceVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )
val CatsVersion    = "3.4.8"
val Http4sVersion  = "0.23.18"
val LogbackVersion = "1.4.7"
val ScoptVersion   = "4.1.0"
val CirceVersion   = "0.14.3"
