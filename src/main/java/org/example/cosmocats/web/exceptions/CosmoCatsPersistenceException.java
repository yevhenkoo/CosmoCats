package org.example.cosmocats.web.exceptions;

public class CosmoCatsPersistenceException extends RuntimeException {
  public CosmoCatsPersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
