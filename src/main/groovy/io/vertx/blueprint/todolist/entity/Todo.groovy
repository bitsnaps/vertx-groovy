package io.vertx.blueprint.todolist.entity

import groovy.json.JsonOutput
import groovy.json.JsonParser
import groovy.json.JsonSlurper
import groovy.transform.Canonical
import groovy.util.logging.Slf4j
import io.vertx.core.json.JsonObject

//import io.vertx.codegen.annotations.DataObject

import java.util.concurrent.atomic.AtomicInteger

@Slf4j
@Canonical
//@DataObject(generateConverter = true)
class Todo {
    private static final AtomicInteger acc = new AtomicInteger(0) // counter

    int id
    String title
    Boolean completed
    Integer order
    String url

    private <T> T getOrElse(T value, T defaultValue) {
        return value == null ? defaultValue : value
    }

    static fromJson(String jsonStr) {
        log.info('****************** jsonStr')
        log.info(jsonStr)
        new Todo(new JsonSlurper().parseText(jsonStr))
    }

    Todo(JsonObject jsonTodo){
//        this.merge(Todo.fromJson(jsonTodo))
//        this.merge(jsonTodo.getMap() as Todo)
        this.id = jsonTodo.getInteger("ID")
        this.title = jsonTodo.getString("TITLE")
        this.completed = jsonTodo.getValue("COMPLETED")
        this.order = jsonTodo.getInteger("ORDER")
        this.url = jsonTodo.getString("URL")
    }

    Todo merge(Todo todo) {
        return new Todo(id,
                getOrElse(todo.title, title),
                getOrElse(todo.completed, completed),
                getOrElse(todo.order, order),
                url)
    }

}
