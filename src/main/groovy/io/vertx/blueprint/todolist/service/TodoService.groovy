package io.vertx.blueprint.todolist.service

import groovy.util.logging.Slf4j
import io.vertx.core.Future
import io.vertx.core.Vertx
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

    Vertx vertx
    JsonObject config
    SQLClient client

    TodoService(Vertx vertx, JsonObject config){
        this.vertx = vertx
        this.config = config
        this.client = JDBCClient.createNonShared(this.vertx, this.config)
    }

    /**
     * Service should either takes a Handler parameter or returns Future.
     */
    Future<Boolean>  initData(){
        Future<Boolean> result = Future.future()
        this.client.getConnection({ conn ->
            if (conn.succeeded()) {
                SQLConnection connection = conn.result()
                connection.query(SQL_CREATE, { res2 ->
                    if (res2.succeeded()) {
                        log.info('*********** Table todo created ************')
                    }
                })
            } else {
                // Failed to get connection - deal with it
                log.info('*********** Cannot create Table todo ************')
            }
        })
        result
    }

}
