package org.example.cosmocats.web.exceptions;

public record ErrorDetails(int status,
                          String error,
                          String message,
                          String path) {}
