import entity.Todo;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.List;


public interface TodoService {

  Completable initData();

  Single<Todo> insert(Todo todo);

  Single<List<Todo>> getAll();

  Maybe<Todo> getCertain(String todoID);

  Maybe<Todo> update(String todoId, Todo newTodo);

  Completable delete(String todoId);

  Completable deleteAll();

  /*
  Future<Boolean> initData(); // init the data (or table)

  Future<Boolean> insert(Todo todo);

  Future<List<Todo>> getAll();

  Future<Optional<Todo>> getCertain(String todoID);

  Future<Todo> update(String todoId, Todo newTodo);

  Future<Boolean> delete(String todoId);

  Future<Boolean> deleteAll();
*/
}