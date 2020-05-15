package io.vertx.blueprint.todolist.verticles

import groovy.util.logging.Slf4j
import io.vertx.blueprint.todolist.entity.Todo
import io.vertx.blueprint.todolist.service.TodoService
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler

import java.util.stream.Collectors

//import java.util.stream.Collectors

/**
 * A Verticle is a component of the application. We can deploy verticles to run the components
 */
@Slf4j
class SingleApplicationVerticle extends AbstractVerticle {

/**
 *
 * Build:
 * gradle build
 *
 * Run:
 * java -jar build/libs/vertx-todo.jar
 *
*/


    static final String HOST = "127.0.0.1"
//    private static final String REDIS_HOST = "127.0.0.1"
    static final int PORT = 8080
//    private static final int REDIS_PORT = 6379 // we do not use Redis here, we've used h2 instead

//    private RedisClient redis;

    TodoService service
    JDBCClient client

    static final String API_GET = "/todos/:todoId"
    static final String API_LIST_ALL = "/todos"
    static final String API_CREATE = "/todos"
    static final String API_UPDATE = "/todos/:todoId"
    static final String API_DELETE = "/todos/:todoId"
    static final String API_DELETE_ALL = "/todos"

    //asynchronous start method
    @Override
    void start(Future<Void> future) throws Exception {
        initData()

        Router router = Router.router(vertx) // The router is responsible for dispatching HTTP requests to the certain right handler
        // CORS support
        Set<String> allowHeaders = new HashSet<>()
        allowHeaders.add("x-requested-with")
        allowHeaders.add("Access-Control-Allow-Origin")
        allowHeaders.add("origin")
        allowHeaders.add("Content-Type")
        allowHeaders.add("accept")
        Set<HttpMethod> allowMethods = new HashSet<>()
        allowMethods.add(HttpMethod.GET)
        allowMethods.add(HttpMethod.POST)
        allowMethods.add(HttpMethod.DELETE)
        allowMethods.add(HttpMethod.PATCH)

        // The route() method with no parameters means that it matches all requests
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods))

        // The BodyHandler allows you to retrieve request bodies and read data
        router.route().handler(BodyHandler.create())

        // routes
        router.get('/').handler({ req ->
            req.response()
                .putHeader("content-type", "text/html")
                .end("Welcome to Todo RestAPI!")
        })

//        router.get(API_GET).handler(this.handleGetTodo())
        router.get(API_LIST_ALL).handler(this.&handleGetAll)
        router.post(API_CREATE).handler(this.&handleCreateTodo)
//        router.patch(API_UPDATE).handler(this.handleUpdateTodo())
//        router.delete(API_DELETE).handler(this.handleDeleteOne())
//        router.delete(API_DELETE_ALL).handler(this.handleDeleteAll())

        vertx.createHttpServer() // create a HTTP server
                .requestHandler({ req ->
            router.accept(req)
        }).listen(PORT, HOST,{ result ->
        if (result.succeeded())
            future.complete()
        else
            future.fail(result.cause())
        })

    }

    void initData() {

        JsonObject config = new JsonObject()
            .put("url","jdbc:h2:mem:~/tododb")
            .put("driver_class","org.h2.Driver")
            .put("user","sa")
            .put("password","")

        this.client =  JDBCClient.createNonShared(vertx, config)
        this.client.getConnection({ conn ->
            if (conn.succeeded()) {
                SQLConnection connection = conn.result()
                connection.query(TodoService.SQL_CREATE, { res2 ->
                    if (res2.succeeded()) {
                        log.info('*********** Table todo created ************')
                    }
                })
            } else {
                // Failed to get connection - deal with it
                log.info('*********** Cannot create Table todo ************')

            }
        })
//        this.service = new TodoService(vertx, config)
//        service.initData()

    }

    private void handleGetTodo(RoutingContext context) {
//        String todoID = context.request().getParam("todoId") // retrieve the path parameter todoId
//        if (todoID == null)
//            sendError(400, context.response()) // the server should send a 400 Bad Request error response to client
//        else {
//            redis.hget({ REDIS_TODO_KEY, todoId, x -> // hget means: get an entry by key from the map (RedisClient hget(String, String, Handler))
//                if (x.succeeded()) {
//                String result = x.result()
//                if (result == null)
//                    sendError(404, context.response())
//                else {
//                    context.response()
//                            .putHeader("content-type", "application/json")
//                            .end(result) // If the result is valid, we could write it to response by end method
//                }
//            } else
//                sendError(503, context.response())
//        })
//        }
    }

    private void handleGetAll(RoutingContext context) {
            this.client.getConnection({ conn ->
            if (conn.succeeded()) {
                SQLConnection connection = conn.result()
                connection.query("SELECT * FROM todo", { res ->
                if (res.succeeded()) {
                    String encoded = Json.encodePrettily(res.result()
                            .getRows(true)
                            .collect{ todo -> new Todo(todo) }
                            .toList())
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(encoded)
                }
            })
            } else {
                // Failed to get connection - deal with it
                sendError(503, context.response())
            }
        })

//        context.response()
//                .putHeader('content-type', 'text/html').write('ok')

    }

    private void handleCreateTodo(RoutingContext context) {
        try {
            //to retrieve JSON data from the request body and decode JSON data to todo entity with the specific constructor
            final String encoded = Json.encodePrettily( Todo.fromJson( context.getBodyAsString()) )
//            JsonArray payload = new JsonArray(encoded)
//            String encoded = "INSERT INTO `todo` " +
//                    "(`title`, `completed`, `order`, `url`) VALUES ('learning', 1, 1, 'http')"
            this.client.getConnection({ conn ->
                if (conn.succeeded()) {
                    SQLConnection connection = conn.result()
                    connection.queryWithParams(TodoService.SQL_INSERT, encoded, { res ->
                        if (res.succeeded())
                            context.response()
                                    // By default Vert.x Web is setting the status to 200 meaning OK. 201 means CREATED
                                    .setStatusCode(201)
                                    .putHeader("content-type", "application/json; charset=utf-8")
                                    .end(encoded)
                        else
                            sendError(503, context.response())
                    })
                } else {
                    // Failed to get connection - deal with it
                    sendError(503, context.response())
                }
            })

        } catch (DecodeException e) {
            sendError(400, context.response())
        }
    }

    // PATCH /todos/:todoId
    /*private void handleUpdateTodo(RoutingContext context) {
        try {
            String todoID = context.request().getParam("todoId") // retrieve the path parameter todoId from the route context
            final Todo newTodo = new Todo(context.getBodyAsString()) // get the new todo entity from the request body
            // handle error
            if (todoID == null || newTodo == null) {
                sendError(400, context.response())
                return
            }

            redis.hget({ REDIS_TODO_KEY, todoId, x -> // retrieve the old todo entity
            if (x.succeeded()) {
                String result = x.result()
                if (result == null)
                    sendError(404, context.response()) // check whether it exists. If not, return 404 Not Found status
                else {
                    Todo oldTodo = new Todo(result);
                    String response = Json.encodePrettily(oldTodo.merge(newTodo)) // merge old todo with new todo and then encode to JSON string
                    redis.hset({ REDISTODOKEY, todo_Id, resp, res -> // if no key matches in Redis, then create; or else update
                    if (res.succeeded()) {
                        context.response()
                                .putHeader("content-type", "application/json")
                                .end(response) // If the operation is successful, write response with 200 OK status
                    }
                })
                }
            } else
                sendError(503, context.response())
        })
        } catch (DecodeException e) {
            sendError(400, context.response())
        }
    }
*/
    /*private void handleDeleteOne(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        redis.hdel({ REDIS_TODO_KEY, todoId, res ->
        if (res.succeeded())
            context.response().setStatusCode(204).end() //Response to the HTTP method delete have generally no content (204 - NO CONTENT)
        else
            sendError(503, context.response())
    });
    }
*/
    /*private void handleDeleteAll(RoutingContext context) {
        redis.del({ REDIS_TODO_KEY, res ->
            if (res.succeeded())
                context.response().setStatusCode(204).end()
            else
                sendError(503, context.response())
            })
    }
*/
    /*/ the id of todos could not be duplicate, so we implement this method
    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.id
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id)
        } else if (id == 0)
            todo.setIncId()
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        return todo
    }
*/
    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end()
    }

/*
// these two are equivalent:
    void doAsync(A a, B b, Handler<R> handler);
    Future<R> doAsync(A a, B b);
// The Future object refers to the result of an action that may not start, or pending, or finish or fail.
    Future<R> future = doAsync(A a, B b);
    future.setHandler(r -> {
        if (r.failed()) {
            // do something on the failure
        } else {
            // do something on the result
        }
    });
*/

}