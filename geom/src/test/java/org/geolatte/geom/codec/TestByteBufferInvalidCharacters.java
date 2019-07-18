package org.geolatte.geom.codec;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.geolatte.geom.ByteBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestByteBufferInvalidCharacters {

  @Parameters
  public static Collection<Object[]> charactersToTest() {
    List<Object[]> characters = new ArrayList<>();
    for (char i = 0; i < 256; ++i) {
      if ((i >= 'A' && i <= 'F')
          || (i >= 'a' && i <= 'f')
          || (i >= '0' && i <= '9')
          || i == '+' || i == '-'
      ) {
        continue;
      }
      characters.add(new Object[]{i});
    }
    return characters;
  }

  private final char characterToTest;

  public TestByteBufferInvalidCharacters(char characterToTest) {
    this.characterToTest = characterToTest;
  }

  @Test(expected = NumberFormatException.class)
  public void testThatNumberFormatExceptionIsThrown() {
    StringBuilder sb = new StringBuilder();
    sb.append(characterToTest);
    sb.append("0");
    ByteBuffer.from(sb.toString());
    fail("string " + sb.toString() + " should trigger a number format exception. characterToTest="
        + (int) characterToTest);
  }

}
