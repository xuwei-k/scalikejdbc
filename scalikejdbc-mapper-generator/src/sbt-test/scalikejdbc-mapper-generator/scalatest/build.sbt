scalikejdbcSettings

val scalikejdbcVersion = System.getProperty("plugin.version")

crossScalaVersions := List("2.11.2", "2.10.4")

scalacOptions ++= Seq("-Xlint", "-language:_", "-deprecation", "-unchecked", "-Xfatal-warnings")

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"      % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-test" % scalikejdbcVersion % "test",
  "org.slf4j"       %  "slf4j-simple"     % "1.7.7",
  "com.h2database"  %  "h2"               % "1.4.177",
  "org.scalatest"   %% "scalatest"        % "2.1.5"            % "test"
)

mainClass := Some("initializer")
