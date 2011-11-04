// set the name of the project

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "1.6.1" % "test" withSources())

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.6.1",
    "org.slf4j" % "slf4j-log4j12" % "1.6.1",
    "log4j" % "log4j" % "1.2.16")

// reduce the maximum number of errors shown by the Scala compiler
maxErrors := 20

// increase the time between polling for file changes when using continuous execution
pollInterval := 1000

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

// set the Scala version used for the project
scalaVersion := "2.9.0-1"

// define the statements initially evaluated when entering 'console', 'console-quick', or 'console-project'
initialCommands := """
  import System.{currentTimeMillis => now}
  def time[T](f: => T): T = {
    val start = now
    try { f } finally { println("Elapsed: " + (now - start)/1000.0 + " s") }
  }
"""

// set the initial commands when entering 'console' only
// initialCommands in console := "import myproject._"

// set Ivy logging to be at the highest level
ivyLoggingLevel := UpdateLogging.Full

// disable updating dynamic revisions (including -SNAPSHOT versions)
offline := true

// set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

// set the prompt (for the current project) to include the username
shellPrompt := { state => System.getProperty("user.name") + "> " }

// disable printing timing information, but still print [success]
// showTiming := false

// disable printing a message indicating the success or failure of running a task
// showSuccess := false

// change the format used for printing task completion time
timingFormat := {
	import java.text.DateFormat
	DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
}

// disable using the Scala version in output paths and artifacts
crossPaths := false

// only use a single thread for building
parallelExecution := false

// Execute tests in the current project serially
//   Tests from other projects may still run concurrently.
parallelExecution in Test := false

// only show 10 lines of stack traces
traceLevel := 20

// only show stack traces up to the first sbt stack frame
traceLevel := 0

publishArtifact in (Compile, packageDoc) := true