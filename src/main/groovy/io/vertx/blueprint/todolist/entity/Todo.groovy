package io.vertx.blueprint.todolist.entity

import groovy.transform.Canonical
//import io.vertx.codegen.annotations.DataObject

import java.util.concurrent.atomic.AtomicInteger

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

    Todo merge(Todo todo) {
        return new Todo(id,
                getOrElse(todo.title, title),
                getOrElse(todo.completed, completed),
                getOrElse(todo.order, order),
                url)
    }

}
