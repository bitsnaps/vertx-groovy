import io.reactivex.Flowable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;


public class HelloVerticle extends AbstractVerticle {

    public static final int PORT = 8080;


    @Override
    public void start(Future<Void> fut) {


        final HttpServer server = vertx.createHttpServer();
//        final Flowable<HttpServerRequest> requestFlowable =  server.requestStream().toFlowable();

        Router router = Router.router(vertx);
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
        });
    }
}
