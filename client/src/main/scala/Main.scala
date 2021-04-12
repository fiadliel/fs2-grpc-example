import cats.effect._
import cats.effect.std.Dispatcher
import com.example.protos.hello._
import fs2._
import io.grpc._
import org.lyranthe.fs2_grpc.java_runtime.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp.Simple {
  val managedChannelStream: Stream[IO, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress("127.0.0.1", 9999)
      .usePlaintext()
      .stream[IO]

  def runProgram(helloStub: GreeterFs2Grpc[IO, Metadata]): IO[Unit] = {
    for {
      response <- helloStub.sayHello(HelloRequest("Joe Bloggs"), new Metadata())
      _ <- IO(println(response.message))
    } yield ()
  }

  val run: IO[Unit] = {
    {for {
      dispatcher <- Stream.resource(Dispatcher[IO])
      managedChannel <- managedChannelStream
      helloStub = GreeterFs2Grpc.stub[IO](dispatcher,managedChannel)
      _ <- Stream.eval(runProgram(helloStub))
    } yield ()}.compile.drain
  }
}
