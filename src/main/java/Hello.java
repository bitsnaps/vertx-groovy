
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Hello extends AbstractVerticle {

//    static class Runner {
//        public static void runExample(Class clazz) {
//            runExample("/src/main/java/" +clazz.getPackage().getName().replace(".", "/"),
//                    clazz.getName(), new VertxOptions().setClustered(false), null);
//        }
//        public static void runExample(String exampleDir, String verticleID, VertxOptions options, DeploymentOptions deploymentOptions) {
//            if (options == null) {
//                // Default parameter
//                options = new VertxOptions();
//            }
//            // Smart cwd detection
//
//            // Based on the current directory (.) and the desired directory (exampleDir), we try to compute the vertx.cwd
//            // directory:
//            try {
//                // We need to use the canonical file. Without the file name is .
//                File current = new File(".").getCanonicalFile();
//                if (exampleDir.startsWith(current.getName()) && !exampleDir.equals(current.getName())) {
//                    exampleDir = exampleDir.substring(current.getName().length() + 1);
//                }
//            } catch (IOException e) {
//                // Ignore it.
//            }
//
//            System.setProperty("vertx.cwd", exampleDir);
//            Consumer<Vertx> runner = vertx -> {
//                try {
//                    if (deploymentOptions != null) {
//                        vertx.deployVerticle(verticleID, deploymentOptions);
//                    } else {
//                        vertx.deployVerticle(verticleID);
//                    }
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                }
//            };
//            if (options.isClustered()) {
//                Vertx.clusteredVertx(options, res -> {
//                    if (res.succeeded()) {
//                        Vertx vertx = res.result();
//                        runner.accept(vertx);
//                    } else {
//                        res.cause().printStackTrace();
//                    }
//                });
//            } else {
//                Vertx vertx = Vertx.vertx(options);
//                runner.accept(vertx);
//            }
//        }
//    }

    public static void main(String[] args){
//        Runner.runExample(Hello.class);
    }

    @Override
    public void start(Future<Void> fut) throws Exception {

//        Router router = Router.router(vertx);
//        router.route().handler(req ->
//            req.response()
//                    .putHeader("content-type", "text/html")
//                    .end("<html><body><h1>Hello from vert.x!</h1></body></html>")
//        );

        vertx.createHttpServer().requestHandler(req -> {
            req.response()
                    .putHeader("content-type", "text/html")
                    .end("<html><body><h1>Hello from vert.x!</h1></body></html>");

        }).listen(8080, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });

    }
}

