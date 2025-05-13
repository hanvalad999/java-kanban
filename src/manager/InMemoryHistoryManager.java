package manager;

import model.Task;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> history = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        // Удаляем старую версию задачи, если она есть
        history.remove(task);

        // Добавляем задачу в конец списка
        history.addLast(task);

        // Ограничиваем размер истории
        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new LinkedList<>(history); // Возвращаем копию
    }
}
