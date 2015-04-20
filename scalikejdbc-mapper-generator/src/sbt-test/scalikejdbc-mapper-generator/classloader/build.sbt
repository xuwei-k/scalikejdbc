scalikejdbcSettings

scalikejdbc.mapper.SbtKeys.scalikejdbcJDBCSettings in Compile := {
  scalikejdbc.mapper.SbtPlugin.JDBCSettings(
    driver = "org.h2.Driver",
    url = "jdbc:h2:./db;MODE=PostgreSQL;AUTO_SERVER=TRUE",
    username = "sa",
    password = "sa",
    schema = ""
  )
}

TaskKey[Unit]("copyh2jar") := {
  val jarName = s"""h2-${System.getProperty("h2.version")}.jar"""
  val jar = file(sys.env("HOME")) / s".ivy2/cache/com.h2database/h2/jars/$jarName"
  IO.copyFile(jar, file("project") / "lib" / jarName)
}

commands += Command.command("setDriverClassLoader"){ state =>
  "set (scalikejdbc.mapper.SbtKeys.scalikejdbcJDBCSettings in Compile) ~= {_.copy(driverClassLoader = Option(classOf[org.h2.Driver].getClassLoader))}" :: state
}

val scalikejdbcVersion = System.getProperty("plugin.version")

scalacOptions ++= Seq("-Xlint", "-language:_", "-deprecation", "-unchecked", "-Xfatal-warnings")

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion,
  "org.slf4j" % "slf4j-simple" % System.getProperty("slf4j.version")
)
