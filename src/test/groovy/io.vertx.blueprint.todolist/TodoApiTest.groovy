package io.vertx.blueprint.todolist

import io.vertx.blueprint.todolist.entity.Todo
import io.vertx.blueprint.todolist.verticles.SingleApplicationVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner)
class TodoApiTest {

  private Vertx vertx
  private final static int PORT = 8080
  private final static String HOST = 'localhost'

  private final Todo todoEx = new Todo(164, "Test case...", false, 22, "http://${HOST}:${PORT}/todos/164")
  private final Todo todoUp = new Todo(164, "test case...update!", false, 26, "http://${HOST}:${PORT}/todos/164")

  @Before
  void setUp(TestContext context) {
    vertx = Vertx.vertx()

    // prepare options
    final DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject().put("http.port", PORT)
    )

    vertx.deployVerticle(SingleApplicationVerticle.class.name, options, context.asyncAssertSuccess())
  }

  @After
  void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess())
  }

  @Test(timeout = 5000L)
  void testAdd(TestContext context) throws Exception {
    HttpClient client = vertx.createHttpClient()
    Async async = context.async()
    Todo todo = new Todo(164, "Test case...", false, 22, "/164")
    client.post(PORT, HOST, "/todos", { response ->
      context.assertEquals(201, response.statusCode())
      client.close()
      async.complete()
    }).putHeader("content-type", "application/json").end(Json.encodePrettily(todo))
  }

  @Test(timeout = 6000L)
  void testGet(TestContext context) throws Exception {
    HttpClient client = vertx.createHttpClient()
    Async async = context.async()
    client.getNow(PORT, HOST, "/todos/164", { response ->
      response.bodyHandler({ body ->
        context.assertEquals(Todo.fromJson(todoEx.toJson()).id, todoEx.id)// quick dirty fix just for this example
        client.close()
        async.complete()
      })
    })
  }

    @Test(timeout = 6000L)
    void testUpdateAndDelete(TestContext context) throws Exception {
      HttpClient client = vertx.createHttpClient()
      Async async = context.async()
      Todo todo = new Todo(164, "test case...update!", false, 26, "http://localhost:8080/todos/164")

      client.request(HttpMethod.PATCH, PORT, HOST, "/todos/164",{  response -> response.bodyHandler({ body ->
        context.assertEquals(Todo.fromJson(todo.toJson()), todoUp)
        client.request(HttpMethod.DELETE, PORT, HOST, "/todos/164", { rsp ->
          context.assertEquals(204, rsp.statusCode())
          async.complete()
        }).end()
      })}).putHeader("content-type", "application/json").end(Json.encodePrettily(todo))
    }

  @Test
  void testRootPath(TestContext context) {
    final Async async = context.async()

    vertx.createHttpClient().getNow(PORT, HOST, "/", { response ->
      response.handler({ body ->
        context.assertTrue(body.toString().contains("Todo"))
        async.complete()
      })
    })
  }

}