package ch.epfl.lsr.sbt.distal
import sbt._
import Keys._

object DistalLocalRunner extends Plugin {
  //import LocalRunnerKeys._
  val basePort = 4000 // TODO: make configurable

  // override lazy val settings = { println("loading the settings"); distalLocalRunnerSettings }

  //object LocalRunnerKeys {
  lazy val distalRunLocal = TaskKey[Unit]("distal-run-local", "starts the protocols locally")
  lazy val distalProtocolsMap = SettingKey[Map[String,Seq[String]]]("distal-protocols-map", "map ports to protocol classes")
  //}


  val distalLocalRunnerSettings = Seq(
    distalRunLocal <<= (streams,distalProtocolsMap,fullClasspath in Runtime, packageBin in Compile) map {
      (out,protocols,classpath :Classpath,packageBin) =>
      val cpString = classpath.map(_.data).mkString(":")
      val locations =
        for {
          ((k,v),i) <- protocols.zipWithIndex
          port = basePort + i
          clazz <- v
        }  yield("lsr://"+clazz+"@localhost:"+port+"/"+k+"/"+clazz)
      val cmdFormat = "java -cp " + cpString + " ch.epfl.lsr.distal.deployment.LocalRunner " + " %s " + " " + locations.mkString(" ")
      val processes = for {
        (k,v) <- protocols
      } yield (cmdFormat.format(k) run out.log)

      processes.foreach(_.exitValue)
      println("done")
    }
    //localProtocolsMap := Map()
  )

}
