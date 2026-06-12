package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4.0, CalculatorEngine.evaluate("2+2"), 0.001)
  }

  @Test
  fun operatorPrecedence_isCorrect() {
    assertEquals(14.0, CalculatorEngine.evaluate("2 + 4 * 3"), 0.001)
    assertEquals(26.0, CalculatorEngine.evaluate("2 * 10 + 3 * 2"), 0.001)
  }

  @Test
  fun parentheses_isCorrect() {
    assertEquals(18.0, CalculatorEngine.evaluate("(2 + 4) * 3"), 0.001)
  }

  @Test
  fun negativeNumbers_isCorrect() {
    assertEquals(-5.0, CalculatorEngine.evaluate("-2 * 3 + 1"), 0.001)
  }

  @Test
  fun modulo_isCorrect() {
    assertEquals(2.0, CalculatorEngine.evaluate("8 % 3"), 0.001)
  }

  @Test
  fun formatResult_isCorrect() {
    assertEquals("5", CalculatorEngine.formatResult(5.0))
    assertEquals("2.5", CalculatorEngine.formatResult(2.5))
  }
}
