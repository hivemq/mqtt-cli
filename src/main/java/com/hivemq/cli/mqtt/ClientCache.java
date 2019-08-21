package com.hivemq.cli.mqtt;

import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientCache<K, V> {

    private final int CACHE_SIZE;
    private final Map<K, Entry<K, V>> CACHE;
    private Entry head;
    private Entry tail;
    private boolean verbose = false;

    public ClientCache(boolean verbose) {
        this();
        this.verbose = verbose;
    }

    ClientCache() {
        this(64);       // default cache size as 64
    }


    private ClientCache(int CACHE_SIZE) {
        this.CACHE_SIZE = CACHE_SIZE;
        CACHE = new HashMap<>(CACHE_SIZE);
    }

    public boolean isVerbose() {
        return verbose;
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public V get(K key) {
        if (CACHE.containsKey(key)) {
            Entry<K, V> entry = CACHE.get(key);
            // remove the recently accessed entry from linkedlist
            remove(entry);
            // and move to top
            splayOnTop(entry);
            if (verbose) Logger.debug("Cache get key {}", key);

            return entry.value;
        }
        return null;
    }

    public boolean hasKey(K key) {
        boolean has = CACHE.containsKey(key);
        if (verbose) Logger.debug("Cache hasKey {} - {} ", key, has);
        return has;
    }

    boolean remove(K key) {
        if (CACHE.containsKey(key)) {
            if (verbose) Logger.debug("Cache remove key {}", key);
            Entry<K, V> entry = CACHE.get(key);
            // remove the recently accessed entry from linked list
            remove(entry);
            return true;
        }
        return false;
    }

    void put(K key, V value) {
        if (CACHE.containsKey(key)) {
            if (verbose) Logger.debug("Cache replace key {}", key);
            Entry<K, V> entry = CACHE.get(key);
            entry.value = value;
            remove(entry);
            splayOnTop(entry);
        } else {
            if (verbose) Logger.debug("Cache put key {}", key);
            Entry<K, V> entry = new Entry<>();
            entry.key = key;
            entry.value = value;
            // reached the cache size, evict the least recently used entry
            if (CACHE.size() == CACHE_SIZE) {
                CACHE.remove(tail.key);
                remove(tail);
            }
            // move the recently accessed entry to top
            splayOnTop(entry);
            CACHE.put(key, entry);
        }
    }

    private Set<Entry<K, V>> entrySet() {
        return new HashSet<>(CACHE.values());
    }

    public Set<K> keySet() {
        return CACHE.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        String delimiter = "";
        for (Entry<K, V> entry : entrySet()) {
            sb.append(delimiter).append(entry.key).append("=").append(entry.value);
            delimiter = ", ";
        }
        sb.append("}");
        return sb.toString();
    }

    private void splayOnTop(Entry<K, V> entry) {
        entry.next = head;
        if (head != null)           // when linkedlist not empty
            head.prev = entry;
        head = entry;
        if (tail == null)           // when first entry
            tail = head;
    }

    private void remove(Entry<K, V> entry) {

        if (entry.prev != null) {
            entry.prev.next = entry.next;
        } else {
            head = entry.next;
        }
        if (entry.next != null) {
            entry.next.prev = entry.prev;
        } else {
            tail = entry.prev;
        }
    }

    private static class Entry<K, V> {
        K key;
        V value;
        Entry prev;
        Entry next;
    }
}

