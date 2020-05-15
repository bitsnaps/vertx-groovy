package io.vertx.blueprint.todolist.verticles

import io.vertx.blueprint.todolist.entity.Todo
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler

//import java.util.stream.Collectors

import static io.vertx.blueprint.todolist.Constants.*

/**
 * A Verticle is a component of the application. We can deploy verticles to run the components
 */
class SingleApplicationVerticle extends AbstractVerticle {

/*
#running process:
gradle build
java -jar build/libs/vertx-todo.jar
*/

    public static final String HTTP_HOST = "127.0.0.1"
//    private static final String REDIS_HOST = "127.0.0.1"
    public static final int PORT = 8080
//    private static final int REDIS_PORT = 6379 // we do not use Redis here, we've used h2 instead

//    private RedisClient redis;

    //asynchronous start method
    @Override
    void start(Future<Void> future) throws Exception {
//        initData()

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

        router.route().handler(CorsHandler.create("*") // The route() method with no parameters means that it matches all requests
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods))

        router.route().handler(BodyHandler.create()) // The BodyHandler allows you to retrieve request bodies and read data

        // routes
        router.get('/').handler({ req ->
            req.response()
                .putHeader("content-type", "text/html")
                .end("Welcome to Todo RestAPI!")
        })

//        router.get(API_GET).handler(this.handleGetTodo())
//        router.get(API_LIST_ALL).handler(this.handleGetAll())
//        router.post(API_CREATE).handler(this.handleCreateTodo())
//        router.patch(API_UPDATE).handler(this.handleUpdateTodo())
//        router.delete(API_DELETE).handler(this.handleDeleteOne())
//        router.delete(API_DELETE_ALL).handler(this.handleDeleteAll())

        vertx.createHttpServer() // create a HTTP server
                .requestHandler({ req ->
            router.accept(req)
        }).listen(PORT, HTTP_HOST,{ result ->
        if (result.succeeded())
            future.complete()
        else
            future.fail(result.cause())
        })

    }

    private void initData() {
/*
        RedisOptions config = new RedisOptions()
                .setHost(config().getString("redis.host", REDIS_HOST))
                .setPort(config().getInteger("redis.port", REDIS_PORT));

        this.redis = RedisClient.create(vertx, config); // create redis client

        redis.hset(Constants.REDIS_TODO_KEY, "24", Json.encodePrettily( // test connection
                new Todo(24, "Something to do...", false, 1, "todo/ex")), res -> {
            if (res.failed()) {
                LOGGER.error("Redis service is not running!");
                res.cause().printStackTrace();
            }
        });
*/
    }

    private void handleGetTodo(RoutingContext context) {
        String todoID = context.request().getParam("todoId") // retrieve the path parameter todoId
        if (todoID == null)
            sendError(400, context.response()) // the server should send a 400 Bad Request error response to client
        else {
            redis.hget({ REDIS_TODO_KEY, todoId, x -> // hget means: get an entry by key from the map (RedisClient hget(String, String, Handler))
                if (x.succeeded()) {
                String result = x.result()
                if (result == null)
                    sendError(404, context.response())
                else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(result) // If the result is valid, we could write it to response by end method
                }
            } else
                sendError(503, context.response())
        })
        }
    }

    private void handleGetAll(RoutingContext context) {
        redis.hvals({REDIS_TODO_KEY, res -> // hvals returns all values in the hash stored at key
        if (res.succeeded()) {
            String encoded = Json.encodePrettily(res.result().stream() // Now we could use Json.encodePrettily method to convert the list to JSON string
                    .map({x -> new Todo((String) x)})
//                    .collect(Collectors.toList()))
                    .collect())
            context.response()
                    .putHeader("content-type", "application/json")
                    .end(encoded) // Finally we write the encoded result to response as before
        } else
            sendError(503, context.response())
    })
    }

    private void handleCreateTodo(RoutingContext context) {
        try {
            final Todo todo = wrapObject(new Todo(context.getBodyAsString()), context) //to retrieve JSON data from the request body and decode JSON data to todo entity with the specific constructor
            final String encoded = Json.encodePrettily(todo)
            redis.hset(REDIS_TODO_KEY, String.valueOf(todo.getId()),
                    encoded, { res ->
            if (res.succeeded())
                context.response()
                        .setStatusCode(201) // By default Vert.x Web is setting the status to 200 meaning OK. 201 means CREATED
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(encoded)
            else
                sendError(503, context.response())
        })
        } catch (DecodeException e) {
            sendError(400, context.response())
        }
    }

    // PATCH /todos/:todoId
    private void handleUpdateTodo(RoutingContext context) {
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

    private void handleDeleteOne(RoutingContext context) {
        String todoID = context.request().getParam("todoId");
        redis.hdel({ REDIS_TODO_KEY, todoId, res ->
        if (res.succeeded())
            context.response().setStatusCode(204).end() //Response to the HTTP method delete have generally no content (204 - NO CONTENT)
        else
            sendError(503, context.response())
    });
    }

    private void handleDeleteAll(RoutingContext context) {
        redis.del({ REDIS_TODO_KEY, res ->
            if (res.succeeded())
                context.response().setStatusCode(204).end()
            else
                sendError(503, context.response())
            })
    }

    // the id of todos could not be duplicate, so we implement this method
    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.id
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id)
        } else if (id == 0)
            todo.setIncId()
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        return todo
    }

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