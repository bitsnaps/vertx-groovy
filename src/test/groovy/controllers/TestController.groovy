package controllers

import io.vertx.ext.web.RoutingContext

class TestController {

	def someMethod(RoutingContext context) {
		context.response().end("Test GET")
	}
}
