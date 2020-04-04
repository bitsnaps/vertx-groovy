
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx

class Hello  {

    static void main(args){
        new Server().start()
    }

}

class Server extends AbstractVerticle {
    void start(){
        def vertx = Vertx.vertx()
        vertx.createHttpServer().requestHandler({ req ->
          req.response()
            .putHeader("content-type", "text/html")
            .end("<html><body><h1>Hello from vert.x!</h1></body></html>")
        }).listen(8080)
    }

}
