import com.example.protos.hello._
import fs2._
import io.grpc._
import cats.effect._

import org.lyranthe.fs2_grpc.java_runtime.syntax.all._

class MyImpl extends GreeterFs2Grpc[IO, Metadata] {
  override def sayHello(request: HelloRequest,
                        clientHeaders: Metadata): IO[HelloReply] = {
    IO(HelloReply("Request name is: " + request.name))
  }

  override def sayHelloStream(
      request: Stream[IO, HelloRequest],
      clientHeaders: Metadata): Stream[IO, HelloReply] = {
    request.evalMap(req => sayHello(req, clientHeaders))
  }
}

object Main extends IOApp.Simple {
  val helloService: Resource[IO, ServerServiceDefinition] =
    GreeterFs2Grpc.bindServiceResource[IO](new MyImpl())


  def run: IO[Unit] = {
    val myFavouriteSync: Async[IO] = Async[IO]

    val startup: IO[Any] = helloService.use{ service =>
      ServerBuilder
        .forPort(9999)
        .addService(service)
        .resource[IO](myFavouriteSync)
        .evalMap(server => IO(server.start()))
        .useForever
    }

    startup >> IO.unit
  }

}
