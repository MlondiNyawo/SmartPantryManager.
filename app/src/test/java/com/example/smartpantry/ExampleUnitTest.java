package com.example.smartpantry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for application logic that does not require the Android runtime.
 */
public class ExampleUnitTest {
    @Test
    public void normaliseName_handlesCaseSpacingAndPlural() {
        assertEquals("tomato", RecipeMatcher.normaliseName("  TOMATOES  "));
    }
}