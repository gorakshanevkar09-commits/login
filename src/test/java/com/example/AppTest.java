package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    void testHello() {
        assertEquals("Hello, Maven POM project!", App.hello());
    }
}
