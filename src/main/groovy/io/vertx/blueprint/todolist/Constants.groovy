package io.vertx.blueprint.todolist

final class Constants {

    static final String REDIS_TODO_KEY = "VERT_TODO"

    /** API Route */
    static final String API_GET = "/todos/:todoId"
    static final String API_LIST_ALL = "/todos"
    static final String API_CREATE = "/todos"
    static final String API_UPDATE = "/todos/:todoId"
    static final String API_DELETE = "/todos/:todoId"
    static final String API_DELETE_ALL = "/todos"

}