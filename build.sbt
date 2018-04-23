
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(LauncherJarPlugin)
  .settings(
    name := "sky-diamond",
    version := "1.0-SNAPSHOT",

  scalaVersion := "2.12.5",
  herokuAppName in Compile := "sky-diamond",

   libraryDependencies ++= Seq(
     jdbc,         // databases
     evolutions,   // track how to set up and destroy tables
	   cacheApi,
     guice,
	   "org.postgresql" % "postgresql" % "42.2.2", // more databases. For postgres.
     "com.h2database" % "h2" % "1.4.193", // still more databases, in-memory, for testing only?
     "org.playframework.anorm" %% "anorm" % "2.6.1", // more databases, unsure how important.

     "com.chuusai" %% "shapeless" % "2.3.3",

      //////// BREEZE ////////
      // Last stable release
      "org.scalanlp" %% "breeze" % "0.13.2",
      // Native libraries are not included by default. add this if you want them (as of 0.7)
      // Native libraries greatly improve performance, but increase jar sizes.
      // It also packages various blas implementations, which have licenses that may or may not
      // be compatible with the Apache License. No GPL code, as best I know.
      "org.scalanlp" %% "breeze-natives" % "0.13.2",
      // The visualization library is distributed separately as well.
      // It depends on LGPL code
      "org.scalanlp" %% "breeze-viz" % "0.13.2",

     "org.typelevel" %% "spire" % "0.14.1" // SPIRE
   ),

    libraryDependencies += scalaVersion("org.scala-lang" % "scala-compiler" % _ ).value
  )
