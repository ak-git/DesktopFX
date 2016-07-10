package com.ak.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractCheckedBytesInterceptor<CHECKED extends Enum<CHECKED> & BytesChecker, RESPONSE, REQUEST extends AbstractBufferFrame>
    extends AbstractBytesInterceptor<RESPONSE, REQUEST> {
  private final ByteBuffer byteBuffer;
  private final Deque<CHECKED> checkedBytes;

  protected AbstractCheckedBytesInterceptor(@Nonnull String name, @Nonnegative int maxCapacity, @Nullable REQUEST pingRequest,
                                            @Nonnull Collection<CHECKED> checkedByteSet) {
    super(name, maxCapacity, pingRequest);
    byteBuffer = ByteBuffer.allocate(maxCapacity);
    checkedBytes = new LinkedList<>(checkedByteSet);
  }

  @Override
  public final int write(@Nonnull ByteBuffer src) {
    src.rewind();
    int countResponse = 0;
    while (src.hasRemaining()) {
      byte b = src.get();
      if (check(b)) {
        byteBuffer.put(b);
      }

      if (!byteBuffer.hasRemaining()) {
        byteBuffer.rewind();
        RESPONSE response = newResponse(byteBuffer);
        if (response != null) {
          bufferPublish().onNext(response);
          countResponse++;
        }
        byteBuffer.clear();
      }
    }
    return countResponse;
  }

  @Nullable
  protected abstract RESPONSE newResponse(@Nonnull ByteBuffer byteBuffer);

  @Override
  protected final void innerPut(@Nonnull ByteBuffer outBuffer, @Nonnull REQUEST request) {
    request.writeTo(outBuffer);
  }

  private boolean check(byte b) {
    boolean ok = true;
    for (CHECKED checkedByte : checkedBytes) {
      if (byteBuffer.position() == checkedByte.ordinal()) {
        if (checkedByte.is(b)) {
          checkedByte.buffer(b, byteBuffer);
        }
        else {
          byteBuffer.clear();
          ok = checkedBytes.getFirst().is(b);
        }
        break;
      }
    }
    return ok;
  }
}
