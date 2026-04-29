package ru.laba5.service;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private final AtomicLong counter = new AtomicLong(1);

    public long nextId() {
        return counter.getAndIncrement();
    }

    // Новый метод: синхронизирует счётчик с максимальным ID из загруженных данных
    public void syncWithMaxId(long maxId) {
        long current = counter.get();
        if (maxId >= current) {
            counter.set(maxId + 1);
        }
    }

    // Для полной синхронизации (если нужно установить конкретное значение)
    public void setNextId(long nextId) {
        if (nextId > 0) {
            counter.set(nextId);
        }
    }
}
