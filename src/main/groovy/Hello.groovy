
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

  static final RethinkDB r = RethinkDB.r

  void start(){
    Connection conn = r.connection().hostname("localhost").port(28015).connect()

    println(r.range(10).coerceTo("array").run(conn))

    // println ( r.range(10).filter({it.mod(2).gt(0)}).map({it.mul(2)}).sum().run(conn) )
    // println ( r.range(10).filter({it.mod(2).gt(0)}).sum().run(conn) )

    // List tables:
    List tables = r.db("test").tableList().run(conn)
    println(tables)
    
    if (!('tv_shows' in tables)) {
      println 'Creating table...'
      r.db("test").tableCreate("tv_shows").run(conn)
      println "Table created!"

      println "Inserting shows..."
      // Java Way
      /*r.table("tv_shows").insert(r.array(
                              r.hashMap("name", "A Great Show!")
                                      .with("rating", 10)
                                      .with("actors", r.array(r.hashMap("name", "Bob"), r.hashMap("name", "Fred"))),
                              r.hashMap("name", "A Terrible Show")
                                      .with("rating", 1)
                                      .with("actors", r.array(r.hashMap("name", "Tom"), r.hashMap("name", "Jim"))),
                              r.hashMap("name", "Ehh")
                                      .with("rating", 5)
                                      .with("actors", r.array(r.hashMap("name", "Bob"), r.hashMap("name", "Alfred")))
                      )).run(conn)*/
      r.table "tv_shows" insert name: "A Great Show!", rating: 10 run conn
      println "Shows inserted!"

    } else {
      println "Table already exists"

      println "Fetching shows..."
      /*/ Java Way:
      Cursor cursor = r.table("tv_shows").run(conn)
      for (Map doc: cursor){
        Map docMap = (Map) doc // not needed for Groovy
        println("Name: ${docMap.get("name")}, Rating: ${docMap.get("rating")}")
        // for (Object actorDoc : ((List) docMap.get("actors"))) {
        //     Map actorDocMap = (Map) actorDoc
        //     println("Actor:")
        //     println("  Name: " + actorDocMap.get("name"))
        // }
      }*/

      r.table "tv_shows" filter {it.getField "rating" eq 10} run conn each {
        println it.name
      }
    }

    println 'Drop table tv_shows...'
    r.db("test").tableDrop("tv_shows").run(conn)
    println 'Table dropped.'

    conn.close()

  } // start()
}
