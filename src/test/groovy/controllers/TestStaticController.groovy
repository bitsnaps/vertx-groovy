package controllers

import io.vertx.ext.web.RoutingContext
import groovy.json.JsonBuilder

class TestStaticController {
	static Closure testClosure = { RoutingContext context ->
		context.response().end(new JsonBuilder([result:"closure"]).toString())
	}
}