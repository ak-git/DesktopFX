package com.ak.digitalfilter;

final class NoFilter extends AbstractOperableFilter {
  @Override
  public int applyAsInt(int in) {
    return in;
  }
}