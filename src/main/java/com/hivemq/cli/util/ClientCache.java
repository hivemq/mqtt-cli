package com.hivemq.cli.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientCache<K, V> {

    private final int CACHE_SIZE;
    private final Map<K, Entry<K, V>> CACHE;
    private Entry<K, V> head, tail;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private boolean verbose = false;

    public ClientCache(boolean verbose) {
        this();
        this.verbose = verbose;
    }

    public ClientCache() {
        this(16);       // default cache size as 16
    }

    public ClientCache(int CACHE_SIZE) {
        this.CACHE_SIZE = CACHE_SIZE;
        CACHE = new HashMap<>(CACHE_SIZE);
    }

    public V get(K key) {
        if (CACHE.containsKey(key)) {
            Entry<K, V> entry = CACHE.get(key);
            // remove the recently accessed entry from linkedlist
            remove(entry);
            // and move to top
            splayOnTop(entry);
            if( verbose) System.out.println("get::" + key);
            return entry.value;
        }
        return null;
    }

    public boolean hasKey(K key) {
        if( verbose) System.out.println("hasKey::" + key +  " - " + CACHE.containsKey(key));
        return (CACHE.containsKey(key));
    }


    public void put(K key, V value) {
        if (CACHE.containsKey(key)) {
            if( verbose) System.out.println("put::" + key);
            Entry<K, V> entry = CACHE.get(key);
            entry.value = value;
            remove(entry);
            splayOnTop(entry);
        } else {
            if( verbose) System.out.println("putIfAbsent::" + key);
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

    public Set<Entry<K, V>> entrySet() {
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

