package com.ak.util;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConcurrentCache<A, V> implements Function<A, V>, Cleaner.Cleanable {
  private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
  private final Function<A, V> c;

  public ConcurrentCache(Function<A, V> c) {
    this.c = Objects.requireNonNull(c);
  }

  @Override
  public V apply(A a) {
    for (; ; ) {
      Future<V> f = cache.get(a);
      if (f == null) {
        Callable<V> callable = () -> c.apply(a);
        RunnableFuture<V> ft = new FutureTask<>(callable);
        f = cache.putIfAbsent(a, ft);
        if (f == null) {
          f = ft;
          ft.run();
        }
      }
      try {
        return f.get();
      }
      catch (CancellationException ex) {
        cache.remove(a, f);
      }
      catch (ExecutionException ex) {
        throw new IllegalStateException("Not unchecked", ex.getCause());
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        Logger.getLogger(getClass().getName()).log(Level.INFO, e, e::getMessage);
      }
      finally {
        f.cancel(true);
      }
    }
  }

  @Override
  public void clean() {
    cache.forEach((a, future) -> future.cancel(true));
  }
}