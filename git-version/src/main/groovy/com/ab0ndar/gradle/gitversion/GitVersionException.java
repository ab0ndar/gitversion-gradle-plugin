package com.ab0ndar.gradle.gitversion;

public class GitVersionException extends IllegalStateException {

  public GitVersionException() {
  }

  public GitVersionException(String s) {
    super(s);
  }

  public GitVersionException(String message, Throwable cause) {
    super(message, cause);
  }

  public GitVersionException(Throwable cause) {
    super(cause);
  }
}
