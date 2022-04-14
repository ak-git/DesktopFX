package com.ak.util;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

public final class ConcurrentCache<A, V> implements Function<A, V>, Cleaner.Cleanable {
  @Nonnull
  private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
  @Nonnull
  private final Function<A, V> c;

  public ConcurrentCache(@Nonnull Function<A, V> c) {
    this.c = Objects.requireNonNull(c);
  }

  @Override
  @Nonnull
  public V apply(@Nonnull A a) {
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