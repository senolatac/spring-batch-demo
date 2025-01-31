package com.aril.arilbatchsdk.core.item;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Setter
public abstract class AbstractPagingServiceAndControllerReader<T> extends AbstractItemCountingItemStreamItemReader<T> {

    protected AtomicInteger page = new AtomicInteger(0);
    protected int pageSize = 100;
    protected AtomicInteger current = new AtomicInteger(0);
    protected List<T> results;
    protected final Lock lock = new ReentrantLock();

    //handles reading items from a data source using paging.
    //It locks resources during execution, checks if a new page is needed, and fetches the next page of data when necessary.
    //The method then returns individual items until the current page is exhausted, after which it moves to the next page.
    @Override
    protected T doRead() throws Exception {
        this.lock.lock();
        try {
            boolean nextPageNeeded = (results != null && current.get() >= Math.max(pageSize, results.size()));

            if (results == null || nextPageNeeded) {

                if (log.isDebugEnabled()) {
                    log.debug("Reading page " + page);
                }

                results = doPageRead();
                page.getAndIncrement();

                if (results.isEmpty()) {
                    return null;
                }

                if (nextPageNeeded) {
                    current.set(0);
                }
            }

            // If no more data, return null to indicate the end of the stream
            if (current.get() < results.size()) {
                T curLine = results.get(current.get());
                current.getAndIncrement();
                return curLine;
            } else {
                return null;
            }
        } finally {
            this.lock.unlock();
        }
    }

    @SuppressWarnings("java:S112")
    protected abstract List<T> doPageRead() throws Exception;

    @Override
    protected void jumpToItem(int itemLastIndex) throws Exception {
        this.lock.lock();
        try {
            page.set(itemLastIndex / pageSize);
            current.set(itemLastIndex % pageSize);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    protected void doOpen() throws Exception {
        //default implementation is empty
    }

    @Override
    protected void doClose() throws Exception {
        this.lock.lock();
        try {
            current.set(0);
            page.set(0);
            results = null;
        } finally {
            this.lock.unlock();
        }
    }
}
