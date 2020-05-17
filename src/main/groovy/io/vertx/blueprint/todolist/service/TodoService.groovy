package io.vertx.blueprint.todolist.service

import groovy.util.logging.Slf4j
import io.vertx.blueprint.todolist.entity.Todo
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection

@Slf4j
class TodoService {

    static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS `todo` (\n" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
            "  `title` varchar(255) DEFAULT NULL,\n" +
            "  `completed` tinyint(1) DEFAULT NULL,\n" +
            "  `order` int(11) DEFAULT NULL,\n" +
            "  `url` varchar(255) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`) )"
    static final String SQL_INSERT = "INSERT INTO `todo` " +
            "(`id`, `title`, `completed`, `order`, `url`) VALUES (?, ?, ?, ?, ?)"
    static final String SQL_QUERY = "SELECT * FROM todo WHERE id = ?"
    static final String SQL_QUERY_ALL = "SELECT * FROM todo"
    static final String SQL_UPDATE = "UPDATE `todo`\n" +
            "SET\n" +
            "`id` = ?,\n" +
            "`title` = ?,\n" +
            "`completed` = ?,\n" +
            "`order` = ?,\n" +
            "`url` = ?\n" +
            "WHERE `id` = ?;"
    static final String SQL_DELETE = "DELETE FROM `todo` WHERE `id` = ?"
    static final String SQL_DELETE_ALL = "DELETE FROM `todo`"

    private final Vertx vertx
    private final JsonObject config
    private final SQLClient client

    TodoService(Vertx vertx, JsonObject config){
        this.vertx = vertx
        this.config = config
        this.client = JDBCClient.create(this.vertx, this.config)
    }

    /**
     * Service should either takes a Handler parameter or returns Future.
     */
    Promise<Boolean>  initData(){
        Promise<Boolean> result = Promise.promise()
        this.client.getConnection({ conn ->
            if (conn.succeeded()) {
                SQLConnection connection = conn.result()
                connection.query(SQL_CREATE, { create ->
                    if (create.succeeded()) {
                        log.info('*********** Table todo created ************')
                        result.complete(true)
                    } else {
                        result.fail(create.cause())
                    }
                    connection.close()
                })
            } else {
                // Failed to get connection - deal with it
                log.info('*********** Cannot create Table todo ************')
            }
        })
        return result
    }

    private Handler<AsyncResult<SQLConnection>> connHandler(Promise future,
                                                            Handler<SQLConnection> handler) {
        return { conn ->
            if (conn.succeeded()) {
                final SQLConnection connection = conn.result()
                handler.handle(connection)
            } else {
                future.fail(conn.cause())
            }
        }
    }

    Promise<Boolean> insert(Todo todo) {
        Promise<Boolean> result = Promise.promise()
        client.getConnection(connHandler(result, {connection ->
            connection.updateWithParams(SQL_INSERT, new JsonArray().add(todo.getId())
                    .add(todo.getTitle())
                    .add(todo.getCompleted())
                    .add(todo.getOrder())
                    .add(todo.getUrl()), {r ->
                if (r.failed()) {
                    result.fail(r.cause())
                } else {
                    result.complete(true)
                }
                connection.close()
            })
        }))
        return result
    }

    Promise<Optional<Todo>> getCertain(String todoID) {
        Promise<Optional<Todo>> result = Promise.promise()
        client.getConnection(connHandler(result, {connection ->
            connection.queryWithParams(SQL_QUERY, new JsonArray().add(todoID), {r ->
                if (r.failed()) {
                    result.fail(r.cause())
                } else {
                    List<JsonObject> rows = r.result().getRows(true)
                    if (rows) {
                        result.complete(Optional.of(Todo.fromJson(rows.first().toString())))
                    } else {
                        result.complete(Optional.empty())
                    }
                }
                connection.close()
            })
        }))
        return result
    }

    Promise<Todo> update(String todoId, Todo newTodo) {
        Promise<Todo> result = Promise.promise()
        client.getConnection(connHandler(result, {connection ->
            this.getCertain(todoId).setHandler({ AsyncResult r ->
                if (r.failed()) {
                    result.fail(r.cause())
                } else {
                    Optional<Todo> oldTodo = r.result()
                    if (!oldTodo.isPresent()) {
                        result.complete(null)
                        return
                    }
                    Todo fnTodo = oldTodo.get().merge(newTodo)
                    int updateId = oldTodo.get().getId()
                    connection.updateWithParams(SQL_UPDATE, new JsonArray().add(updateId)
                            .add(fnTodo.getTitle())
                            .add(fnTodo.getCompleted())
                            .add(fnTodo.getOrder())
                            .add(fnTodo.getUrl())
                            .add(updateId), {x ->
                        if (x.failed()) {
                            result.fail(r.cause())
                        } else {
                            result.complete(fnTodo)
                        }
                        connection.close()
                    })
                }
            })
        }))
        return result
    }

    Promise<List<Todo>> getAll(){
        Promise<List<Todo>> result = Promise.promise()
        this.client.getConnection(connHandler(result, { conn ->
                conn.query(SQL_QUERY_ALL, { res ->
                    if (res.failed()){
                        result.fail(res.cause())
                    } else {
                        List<Todo> todos = res.result()
                                .getRows(true)
                                .collect{ x -> Todo.fromJson(x.toString()) }
                                .toList()
                        result.complete( todos )
                    }
                })
            conn.close()
        }))
        result
    }

    private Promise<Boolean> deleteProcess(String sql, JsonArray params) {
        Promise<Boolean> result = Promise.promise()
        client.getConnection(connHandler(result, {connection ->
            connection.updateWithParams(sql, params, {r ->
                result.complete(r.failed())
                connection.close()
            })
        }))
        return result
    }

    Promise<Boolean> delete(String todoId) {
        return deleteProcess(SQL_DELETE, new JsonArray().add(todoId))
    }

    Promise<Boolean> deleteAll() {
        return deleteProcess(SQL_DELETE_ALL, new JsonArray())
    }

}
