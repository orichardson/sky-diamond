resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.11") // play plugin, 2.4.3 default, 2.6.13 latest
addSbtPlugin("com.heroku" % "sbt-heroku" % "1.0.1")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.11") // compile sass.