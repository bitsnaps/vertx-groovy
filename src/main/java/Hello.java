
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Hello extends AbstractVerticle {

/*
  // https://knative.dev/community/samples/serving/helloworld-vertx/
  public void start() {

         final HttpServer server = vertx.createHttpServer();
         final Flowable<HttpServerRequest> requestFlowable = server.requestStream().toFlowable();

         requestFlowable.subscribe(httpServerRequest -> {

             String target = System.getenv("TARGET");
             if (target == null) {
                 target = "NOT SPECIFIED";
             }

             httpServerRequest.response().setChunked(true)
                     .putHeader("content-type", "text/plain")
                     .setStatusCode(200) // OK
                     .end("Hello World: " + target);
         });

         server.listen(8080);
     }
*/

//    public static void main(String[] args){
//    }

    @Override
    public void start(Future<Void> fut) throws Exception {

        Router router = Router.router(vertx);
//        router.route().handler(req ->
//            req.response()
//                    .putHeader("content-type", "text/html")
//                    .end("<html><body><h1>Hello from vert.x!</h1></body></html>")
//        );

        vertx.createHttpServer().requestHandler(req -> {
            req.response()
                    .putHeader("content-type", "text/html")
                    .end("<html><body><h1>Hello from vert.x!</h1></body></html>");

        }).listen(8080, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });

    }
}

