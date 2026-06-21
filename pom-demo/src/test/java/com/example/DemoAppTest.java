package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DemoAppTest {
    @Test
    void greetReturnsExpectedMessage() {
        assertEquals("Hello, Maven!", DemoApp.greet("Maven"));
    }
}
