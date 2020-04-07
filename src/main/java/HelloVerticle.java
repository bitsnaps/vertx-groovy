import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;


public class HelloVerticle extends AbstractVerticle {

    public static final int PORT = 8080;


    @Override
    public void start(Future<Void> fut) {

        final HttpServer server = vertx.createHttpServer();
        final Flowable<HttpServerRequest> requestFlowable =  server.requestStream().toFlowable();

        requestFlowable.subscribe(req -> {
            req.response()
                    .setChunked(true)
                    .putHeader("content-type", "text/plain")
                    .setStatusCode(200)
                    .end("Hello RxVertx!");
        });

        server.listen(PORT);

        // package: io.vertx.core.* instead of io.vertx.reactivex.core.
/*        Router router = Router.router(vertx);
        // HelloVerticle
//        server.requestHandler(req -> {
        router.route().handler(req -> {
            req.response()
                    .putHeader("content-type", "text/html")
                    .end("<html><body><h1>Hello vert.x!</h1><br><p>Date: " + DateHelper.today() + "</p></body></html>");

        });

        server.requestHandler(router::accept).listen(PORT, http -> {
            if (http.succeeded()) {
                fut.complete();
                System.out.println("HTTP server started on port "+PORT);
            } else {
                fut.fail(http.cause());
            }
        });*/

    }
}
