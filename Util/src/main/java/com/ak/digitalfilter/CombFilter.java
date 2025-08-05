package com.ak.digitalfilter;

final class CombFilter extends AbstractBufferFilter {
  CombFilter(int combFactor) {
    super(combFactor + 1);
  }

  @Override
  int apply(int nowIndex) {
    return get(nowIndex) - get(nowIndex + 1);
  }
}