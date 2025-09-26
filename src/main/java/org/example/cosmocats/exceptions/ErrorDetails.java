package org.example.cosmocats.exceptions;

public record ErrorDetails(int status,
                          String error,
                          String message,
                          String path) {}
