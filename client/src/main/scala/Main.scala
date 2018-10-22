import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.example.protos.hello._
import io.grpc._
import org.lyranthe.fs2_grpc.java_runtime.implicits._
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {
  val managedChannelResource: Resource[IO, ManagedChannel] =
    ManagedChannelBuilder
      .forAddress("127.0.0.1", 9999)
      .usePlaintext()
      .resource

  def runProgram(helloStub: GreeterFs2Grpc[IO]): IO[Unit] = {
    for {
      response <- helloStub.sayHello(HelloRequest("John Doe"), new Metadata())
      _ <- IO(println(response.message))
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    managedChannelResource.use { managedChannel =>
      val helloStub = GreeterFs2Grpc.stub[IO](managedChannel)
      runProgram(helloStub)
    }.as(ExitCode.Success)
  }
}
