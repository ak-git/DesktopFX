package com.ak.digitalfilter;

final class NoFilter extends OperableFilter {
  @Override
  public int applyAsInt(int in) {
    return in;
  }
}