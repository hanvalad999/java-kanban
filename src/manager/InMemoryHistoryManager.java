package manager;

import model.Task;


import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task, Node prew, Node next) {
            this.task = task;
            this.prev = prew;
            this.next = next;
        }
    }

    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (history.containsKey(task.getId())) {
            remove(task.getId());
        }

        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = history.remove(id);
        if (node == null) return;

        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task, tail, null);

        if (tail != null) {
            tail.next = newNode;
        } else {
            head = newNode;
        }

        tail = newNode;
        history.put(task.getId(), newNode);
    }

}


