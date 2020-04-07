
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;


// Way 2
public class Hello extends AbstractVerticle {

    public static void main(String[] args) {
        new Hello().start();
    }

    @Override
    public void start(
            //Future<Void> fut
    ) {

        // Hello
        Vertx.vertx().createHttpServer().requestHandler(req -> {
            req.response()
                    .putHeader("content-type", "text/html")
                    .end("<html><body><h1>Hello from vert.x!</h1><br><p>Date: " + DateHelper.today() + "</p></body></html>");

        }).listen(8080
//                , result -> {
//            if (result.succeeded()) {
//                fut.complete();
//            } else {
//                fut.fail(result.cause());
//            }
//        }
        );
    }
}
