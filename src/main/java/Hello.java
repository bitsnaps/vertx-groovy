
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.function.Consumer;

public class Hello extends AbstractVerticle {

//    public static void main(String[] args){
//    }

    @Override
    public void start(Future<Void> fut) throws Exception {

        Router router = Router.router(vertx);

        router.route().handler(req ->
            req.response()
                    .putHeader("content-type", "text/html")
                    .end("<html><body><h1>Hello vert.x!</h1><p>"+new Date().toString()+"</p></body></html>")
        );

        vertx.createHttpServer().requestHandler(handler -> router.accept(handler)).listen(8080, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });

    }
}

