Global / onChangedBuildSource := ReloadOnSourceChanges

val Scala3 = "3.0.0-RC2"
val Scala213 = "2.13.5"

val grpcVersion = "1.37.0"
val googleProtoVersion = "3.15.7"

ThisBuild / scalaVersion := Scala213 

lazy val root = project.in(file("."))
  .settings(
    publish / skip := true
  )
  .aggregate(protobuf, client, server)

val protobuf =
  project
    .in(file("protobuf"))
    .settings(
      libraryDependencies ++= Seq(
        "com.google.protobuf" % "protobuf-java" % googleProtoVersion  % "protobuf"
      )
    )
    .enablePlugins(Fs2Grpc)
lazy val client =
  project
    .in(file("client"))
    .settings(
      libraryDependencies ++= Seq(
         "io.grpc" % "grpc-netty" % grpcVersion
      ),
      scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
    )
    .dependsOn(protobuf)
    .dependsOn(protobuf % "protobuf")
    .enablePlugins(Fs2Grpc)

lazy val server =
  project
    .in(file("server"))
    .settings(
      scalaVersion := "2.13.5",
      libraryDependencies ++= List(
        "io.grpc" % "grpc-netty" % grpcVersion,
        "io.grpc" % "grpc-services" % grpcVersion
      ),
      scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage,
    )
    .dependsOn(protobuf)
    .dependsOn(protobuf % "protobuf")
    .enablePlugins(Fs2Grpc)

