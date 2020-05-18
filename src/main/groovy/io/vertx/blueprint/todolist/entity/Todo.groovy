package io.vertx.blueprint.todolist.entity

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Canonical
import io.vertx.core.json.JsonArray

import java.util.concurrent.atomic.AtomicInteger

@Canonical
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

    static Todo fromJson(String jsonStr) {
        // .getRows(true) from ResultSet didn't work, so I'm converting everything to lowercase
        new Todo(new JsonSlurper().parseText(jsonStr.toLowerCase()))
    }

    JsonArray toJsonArray(){
        new JsonArray([this.id, this.title, this.completed, this.order, this.url])
    }

    String toJson(){
        JsonOutput.toJson(this)
    }

    Todo merge(Todo todo) {
        return new Todo(id,
                getOrElse(todo.title, title),
                getOrElse(todo.completed, completed),
                getOrElse(todo.order, order),
                getOrElse(todo.url, url)
        )
    }

}
