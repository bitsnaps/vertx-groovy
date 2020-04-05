package tests

import io.vertx.core.buffer.Buffer

import static org.junit.Assert.*
import groovy.json.JsonBuilder
import groovy.transform.TypeChecked
import io.vertx.core.http.HttpClientResponse
import io.vertx.ext.unit.Async
import io.vertx.ext.unit.TestContext
import org.junit.Test
import static io.vertx.core.http.HttpHeaders.*

@TypeChecked
class RoutingTest extends TestBase {

	@Test
	public void testGetHandler(TestContext context) {
		Async async = context.async()
		client().get("/handlers", { HttpClientResponse response ->
			assertEquals(200, response.statusCode())
			response.bodyHandler { Buffer buffer ->
				context.assertEquals(buffer.toString('UTF-8'), new JsonBuilder([result:"GET"]).toString())
				async.complete()
			}
		})
		.putHeader(ACCEPT.toString(), "application/json")
		.putHeader(CONTENT_TYPE.toString(), "application/json")
		.end()
	}

	@Test
	public void testWrongContentType(TestContext context) {
		Async async = context.async()
		client().get("/handlers", { HttpClientResponse response ->
			context.assertEquals(response.statusCode(), 404)
			async.complete()
		})
		.putHeader(ACCEPT.toString(), "application/xml")
		.putHeader(CONTENT_TYPE.toString(), "application/xml")
		.end()
	}

	@Test
	void testPostHandler(TestContext context) {
		Async async = context.async()
		client().post("/handlers", { HttpClientResponse response ->
			context.assertEquals(response.statusCode(), 200)
			response.bodyHandler { Buffer buffer ->
				context.assertEquals(buffer.toString("UTF-8"), new JsonBuilder([result:"POST"]).toString())
				async.complete()
			}
		})
		.putHeader(ACCEPT.toString(), "application/json")
		.putHeader(CONTENT_TYPE.toString(), "application/json")
		.end()
	}

	@Test
	void testGetStatic(TestContext context) {
		Async async = context.async()
		client().get("/staticClosure", { HttpClientResponse response ->
			assertEquals(200, response.statusCode())
			response.bodyHandler { Buffer buffer ->
				context.assertEquals(buffer.toString("UTF-8"), new JsonBuilder([result:"closure"]).toString())
				async.complete()
			}
		})
		.end()
	}
}