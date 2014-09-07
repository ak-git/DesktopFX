package com.ak.comm.serial;

import java.util.Deque;
import java.util.LinkedList;

import gnu.io.CommPortIdentifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class ComServiceUtilsTest {
  @Test
  public void testGetPorts() {
    Deque<String> ids = new LinkedList<>();
    for (; ; ) {
      CommPortIdentifier identifier = ComServiceUtils.PORTS.next("");
      if (identifier == null) {
        break;
      }
      else if (!ids.isEmpty() && identifier.getName().equals(ids.getFirst())) {
        break;
      }
      ids.add(identifier.getName());
    }

    ids.stream().filter(id -> !id.isEmpty()).forEach(id ->
        Assert.assertEquals(ComServiceUtils.PORTS.next(id).getName(), ComServiceUtils.PORTS.next(id).getName()));
  }
}