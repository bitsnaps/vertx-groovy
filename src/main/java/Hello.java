import entity.Todo;
import io.reactivex.Completable;
import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

import java.util.Objects;

//import io.vertx.ext.web.Router;
//import io.vertx.ext.web.RoutingContext;
//import io.vertx.ext.web.handler.BodyHandler;
//import io.vertx.ext.web.handler.CorsHandler;


public class Hello extends RestfulApiVerticle {
    /**
     *
     * CREATE:
     *  curl -d '{"title":"Learn Vert.x","completed":true,"order":1}' -H "Content-Type: application/json" http://127.0.0.1:8080/todos
     *  curl -d '{"title":"Learn Vert.x with RxJava2","completed":true,"order":2}' -H "Content-Type: application/json" http://127.0.0.1:8080/todos
     *
     * READ:
     * curl http://localhost:8080/todos/2
     *
     * UPDATE:
     * curl -X PATCH -d '{"title":"Learn RxJava"}' -H "Content-Type: application/json" http://localhost:8080/todos/1
     *
     * DELETE:
     * curl -X DELETE http://localhost:8080/todos/1
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(Hello.class);

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 8080;
    private TodoService service;

//    public static void main(String[] args){
//    }

    @Override
    public void start(Future<Void> future) throws Exception {

        Router router = Router.router(vertx);

        // Enable HTTP Body parse.
        router.route().handler(BodyHandler.create());
        // Enable CORS.
        enableCorsSupport(router);

        // routes
        router.get(Constants.API_GET).handler(this::handleGetTodo);
        router.get(Constants.API_LIST_ALL).handler(this::handleGetAll);
        router.post(Constants.API_CREATE).handler(this::handleCreateTodo);
        router.patch(Constants.API_UPDATE).handler(this::handleUpdateTodo);
        router.delete(Constants.API_DELETE).handler(this::handleDeleteOne);
        router.delete(Constants.API_DELETE_ALL).handler(this::handleDeleteAll);

        String host = config().getString("http.address", HOST);
        int port = config().getInteger("http.port", PORT);

        initService().andThen(createHttpServer(router, host, port))
                .subscribe(future::complete, future::fail);

//        vertx.createHttpServer()
//                .requestHandler(router::accept)
//                .listen(PORT, HOST, result -> {
//                    if (result.succeeded())
//                        future.complete();
//                    else
//                        future.fail(result.cause());
//                });
//
//        initData();

/*
        JsonObject config = new JsonObject()
                .put("url","jdbc:h2:mem:~/test")
                .put("driver_class","org.h2.Driver")
                .put("user","sa")
                .put("password","sa");

        SQLClient client = this.getSqlClient(config, "tododb");

        router.route().handler(req -> {

            client.getConnection(conn -> {
                if (conn.succeeded()) {

                    SQLConnection connection = conn.result();

                    connection.query("SELECT 1+1", res2 -> {
                        if (res2.succeeded()) {

                            ResultSet rs = res2.result();
                            // Got results from ResultSet
                            req.response()
                                    .putHeader("content-type", "text/html")
                                    .end("Data: " + rs.getRows().get(0).toString());
                        }
                    });
                } else {
                    // Failed to get connection - deal with it
                    req.response()
                            .putHeader("content-type", "text/html")
                            .end("Connect failed.");

                }
            });

            }
        );

        vertx.createHttpServer().requestHandler(handler -> router.accept(handler)).listen(8080, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });*/

    }

/*    private SQLClient getSqlClient(JsonObject config, String dbName ){//, DataSource datasource){

        // Using default shared data source
        SQLClient client = JDBCClient.createShared(vertx, config, dbName);

        // Creating a client with a non shared data source
//        SQLClient client = JDBCClient.createNonShared(vertx, config);
//        SQLClient client = JDBCClient.create(vertx, datasource);
        return client;
    }*/

    private void handleCreateTodo(RoutingContext context) {
        try {
            JsonObject rawEntity = context.getBodyAsJson();
            if (!Objects.isNull(rawEntity)) {
                final Todo todo = wrapObject(new Todo(rawEntity), context);
                // Call async service then send response back to client.
                sendResponse(context, service.insert(todo), Json::encodePrettily, this::created);
                return;
            }
            badRequest(context);
        } catch (DecodeException ex) {
            badRequest(context, ex);
        }
    }

    private void handleGetTodo(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        if (todoID == null) {
            badRequest(context);
            return;
        }
        sendResponse(context, service.getCertain(todoID), Json::encodePrettily);
    }

    private void handleGetAll(RoutingContext context) {
        sendResponse(context, service.getAll(), Json::encodePrettily);
    }

    private void handleUpdateTodo(RoutingContext context) {
        try {
            String todoID = context.request().getParam("todoId");
            final Todo newTodo = new Todo(context.getBodyAsString());
            // handle error
            if (todoID == null) {
                badRequest(context);
                return;
            }
            sendResponse(context, service.update(todoID, newTodo), Json::encodePrettily);
        } catch (DecodeException ex) {
            badRequest(context, ex);
        }
    }

    private void handleDeleteOne(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        sendResponse(context, service.delete(todoID), this::noContent);
    }

    private void handleDeleteAll(RoutingContext context) {
        sendResponse(context, service.deleteAll(), this::noContent);
    }

    private Completable initService() {
        JsonObject config = new JsonObject()
                .put("url","jdbc:h2:mem:~/tododb")
                .put("driver_class","org.h2.Driver")
                .put("user","sa")
                .put("password","");

        service = new JdbcTodoService(vertx, config);
        return service.initData();
    }


    /**
     * Wrap the todo entity with appropriate id and URL.
     *
     * @param todo    a todo entity
     * @param context RoutingContext
     * @return the wrapped todo entity
     */
    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.getId();
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id);
        } else if (id == 0)
            todo.setIncId();
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        return todo;
    }

}

