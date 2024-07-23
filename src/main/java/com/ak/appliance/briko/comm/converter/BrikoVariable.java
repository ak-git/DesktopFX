package com.ak.appliance.briko.comm.converter;

import com.ak.comm.converter.Variable;

public enum BrikoVariable implements Variable<BrikoVariable> {
  EMG1,
  EMG2,
  EMG3,
  EMG4,
  EMG5,
  EMG6,
  EMG7,
  EMG8,
  EMG9,
  EMG10,
  EMG11,
  EMG12,

  R1,
  R2,
  R3,
  R4,
  R5,
  R6,
  R7,
  R8,
  R9,
  R10,
  R11,
  R12,

  MTG1,
  MTG2,
  MTG3,
  MTG4,
  MTG5,
  MTG6,
  MTG7,
  MTG8,
  MTG9,
  MTG10,
  MTG11,
  MTG12,

  AX,
  AY,
  AZ,

  GX,
  GY,
  GZ;

  public static final int FREQUENCY = 512;
}
