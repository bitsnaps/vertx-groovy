
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import com.rethinkdb.RethinkDB
import com.rethinkdb.net.Connection
import com.rethinkdb.net.Cursor

class Hello  {

    static void main(args){
        new Server().start()
    }

}

class Server extends AbstractVerticle {

  void start(){
    RethinkDB r = RethinkDB.r
    Connection conn = r.connection().hostname("localhost").port(28015).connect()
    println(r.range(10).coerceTo("array").run(conn))
  }
}

/*
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
*/
