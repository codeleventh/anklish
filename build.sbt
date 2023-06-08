val Http4sVersion          = "0.23.18"
lazy val root = (project in file("."))
  .settings(
    organization := "ru.eleventh",
    name         := "anklish",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.9",
    scalacOptions ++= Seq("-Ymacro-annotations"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.github.scopt" % "scopt_2.13" % ScoptVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-generic-extras" % CirceVersion,
      "com.beachape" %% "enumeratum" % EnumeratumVersion,
      "com.beachape" %% "enumeratum-circe" % EnumeratumVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
val CirceVersion = "0.14.5"
val EnumeratumVersion = "1.7.2"
val MunitCatsEffectVersion = "1.0.7"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.4.7"
val ScoptVersion = "4.1.0"
