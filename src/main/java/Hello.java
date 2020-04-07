
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/*/ Way 1
public class Hello {

    public static void main(String[] args) {
        Vertx.vertx().createHttpServer().requestHandler(req ->
                req.response()
                        .putHeader("content-type", "text/html")
                        .end("<html><body><h1>Hello from vert.x!</h1><br><p>Date: " + DateHelper.today() + "</p></body></html>")
        ).listen(8080);
    }
}*/

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

/*/ Way 3
public class Hello {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        // set method for a route
//        Route route = Router.router(vertx).route().method(HttpMethod.GET);

        // handler any request
//        router.route().handler(req ->
        // handle get "/" request
        router.route("/:name")
//                .consumes("application/json") // consume only messages of type json (you can replace 'application' by '*'
                .handler( reqCtx -> {
            HttpServerRequest req = reqCtx.request();
            String name = req.getParam("name").isEmpty()?"User":req.getParam("name");
            reqCtx.response()
                    .putHeader("content-type", "text/html")
                    .end("<html><body><h1>Welcome "+name+"</h1><h1>Hello vert.x!</h1><p>"+DateHelper.today()+"</p></body></html>");
    });


        // Multiple handlers
//        Route hanlder1 = router.get("/").handler(req -> {
//            req.response()
//                .setChunked(true) // stream the data (reactive)
//                .write("<h1>Welcome "+name+", Please wait, processing...</h1>");
//            req.vertx().setTimer(3000, tid -> {
//                req.next();
//            });
//        });
//
//        Route hanlder2 = router.get("/").handler(req -> {
//            req.response()
//                .write("<h2>You're close, almost done...</h2>");
//            req.vertx().setTimer(2000, tid -> {
//                req.next();
//            });
//        });
//
//        Route hanlder3 = router.get("/").handler(req -> {
//            req.response()
//                .end("<h3>Thank your for waiting.</h3>");
//        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
*/