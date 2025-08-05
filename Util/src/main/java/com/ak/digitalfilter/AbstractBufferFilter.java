package com.ak.digitalfilter;

abstract class AbstractBufferFilter extends AbstractOperableFilter {
  private final int[] buffer;
  private int bufferIndex = -1;

  AbstractBufferFilter(int size) {
    buffer = new int[size];
  }

  @Override
  public double getDelay() {
    return (length() - 1) / 2.0;
  }

  @Override
  public final int applyAsInt(int in) {
    bufferIndex = (++bufferIndex) % buffer.length;
    buffer[bufferIndex] = in;
    return apply(bufferIndex);
  }

  final int get(int index) {
    return buffer[index % buffer.length];
  }

  final int[] buffer() {
    return buffer.clone();
  }

  final int length() {
    return buffer.length;
  }

  abstract int apply(int nowIndex);
}