import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)

public class HelloVerticleTest {

  @Test
  void start_server() {

    Vertx vertx = Vertx.vertx();

    vertx.createHttpServer()
            .requestHandler(req -> req.response().end("Ok"))
            .listen(8080, ar -> {
              // (we can check here if the server started or not)
            });
  }

}