package io.vertx.blueprint.todolist.verticles

import groovy.util.logging.Slf4j
import io.vertx.blueprint.todolist.entity.Todo
import io.vertx.blueprint.todolist.service.TodoService
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler

import java.util.function.Consumer

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
    static final int PORT = 8080

    TodoService service

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

        router.get(API_GET).handler(this.&handleGetTodo)
        router.get(API_LIST_ALL).handler(this.&handleGetAll)
        router.post(API_CREATE).handler(this.&handleCreateTodo)
        router.patch(API_UPDATE).handler(this.&handleUpdateTodo)
        router.delete(API_DELETE).handler(this.&handleDeleteOne)
        router.delete(API_DELETE_ALL).handler(this.&handleDeleteAll)

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
        this.service = new TodoService(vertx, config)

        service.initData().setHandler({res ->
            if (res.failed()){
                log.error('Persistence service is not running!')
                res.cause().printStackTrace()
            }
        })
    }

    /**
     * Wrap the result handler with failure handler (503 Service Unavailable)
     */
    private <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Consumer<T> consumer) {
        return { Future<T> res ->
            if (res.succeeded()) {
                consumer.accept(res.result())
            } else {
                serviceUnavailable(context)
            }
        }
    }

    /**
     * curl localhost:8080/todos/1
     * @param context
     */
    private void handleGetTodo(RoutingContext context) {
        // retrieve the path parameter todoId
        String todoID = context.request().getParam("todoId")
        if (todoID == null)
            // the server should send a 400 Bad Request error response to client
            sendError(400, context.response())
        else {
            this.service.getCertain(todoID).setHandler(resultHandler(context, { Optional<Todo> res ->
                if (!res.isPresent())
                    sendError(404, context.response())
                else {
                    String encoded = Json.encodePrettily(res.get())
                    context.response()
                        .putHeader("content-type", "application/json")
                        .end(encoded)
            }
            }))
        }
    }

    /**
     * curl localhost:8080/todos
     * @param context
     */
    private void handleGetAll(RoutingContext context) {
        this.service.getAll().setHandler(resultHandler(context, { res ->
            if (res == null){
                serviceUnavailable(context)
            } else {
//                String encoded = Json.encodePrettily(res)
                String encoded = (res as List<Todo>)*.toJson().collect().toString()
                context.response()
                    .putHeader("content-type", "application/json")
                    .end(encoded)
            }
        }))
            /*this.service.client.getConnection({ conn ->
            if (conn.succeeded()) {
                SQLConnection connection = conn.result()
                connection.query(TodoService.SQL_QUERY_ALL, { res ->
                    if (res.succeeded()) {
                        String encoded = Json.encodePrettily(res.result()
                                .getRows(true)
                                .collect{ x -> Todo.fromJson(x.toString()) }
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
        })*/
    }

    /**
     * curl -d '{"id":1,"title":"Learn Vert.x","completed":true,"order":1,"url":"http://vertx.org"}' -H "Content-Type: application/json" http://127.0.0.1:8080/todos
     * curl -d '{"id":2,"title":"Learn RxJS","completed":false,"order":1,"url":"http://rxjs.org"}' -H "Content-Type: application/json" http://127.0.0.1:8080/todos
     * @param context
     */
    private void handleCreateTodo(RoutingContext context) {
        try {
            //to retrieve JSON data from the request body and decode JSON data to todo entity with the specific constructor
            Todo todo = Todo.fromJson(context.getBodyAsString())
            this.service.insert(todo).setHandler(resultHandler(context, { res ->
                if (res)
                    context.response()
                            // By default Vert.x Web is setting the status to 200 meaning OK. 201 means CREATED
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(todo.toJson())
                else
                    sendError(503, context.response())

            }))

        } catch (DecodeException e) {
            sendError(400, context.response())
        }
    }

    /**
     * curl -X PATCH -d '{"id":1,"title":"Learn RxJS","completed":false,"order":2,"url":"http://rxjs.org"}' -H "Content-Type: application/json" http://127.0.0.1:8080/todos/1
     * @param context
     */
    private void handleUpdateTodo(RoutingContext context) {
        try {
            // retrieve the path parameter todoId from the route context
            String todoID = context.request().getParam("todoId")
            // get the value entity from the request body
            final Todo newTodo = Todo.fromJson(context.getBodyAsString())
            // handle error
            if (todoID == null || newTodo == null) {
                sendError(400, context.response())
                return
            }
            this.service.update(todoID, newTodo).setHandler(resultHandler(context, { res ->
                if (res == null){
                    sendError(404, context.response())
                } else {
                    context.response()
                        .putHeader("content-type", "application/json")
                        // If the operation is successful, write response with 200 OK status
                        .end(newTodo.toJson())
                }
            }))
        } catch (DecodeException e) {
            sendError(400, context.response())
        }
    }

    /**
     * curl -X DELETE localhost:8080/todos/1
     * @param context
     */
    private void handleDeleteOne(RoutingContext context) {
        String todoID = context.request().getParam("todoId")
        this.service.delete(todoID).setHandler(resultHandler(context, { res ->
            if (res != null) {
                //Response to the HTTP method delete have generally no content (204 - NO CONTENT)
                context.response().setStatusCode(204).end()
            } else {
                // check whether it exists. If not, return 404 Not Found status
                sendError(404, context.response())
            }
        }))
    }

    /**
     * curl -X DELETE localhost:8080/todos
     * @param context
     */
    private void handleDeleteAll(RoutingContext context) {
        this.service.deleteAll().setHandler(resultHandler(context, { res ->
            if (res != null) {
                context.response().setStatusCode(204).end()
            } else {
                sendError(404, context.response())
            }
        }))
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end()
    }

    private void serviceUnavailable(HttpServerResponse response) {
        response.setStatusCode(503).end()
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