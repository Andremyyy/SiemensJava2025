package com.siemens.internship.exceptions;

public class ProcessingException extends RuntimeException {
  public ProcessingException(String message) {
    super(message);
  }

  public ProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
