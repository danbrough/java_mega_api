package org.danbrough.mega;

import java.util.ResourceBundle;

public enum APIError {
  API_OK(0), // no error
  API_EINTERNAL(-1), // internal error
  API_EARGS(-2), // bad arguments
  API_EAGAIN(-3), // request failed, retry with exponential backoff
  API_ERATELIMIT(-4), // too many requests, slow down
  API_EFAILED(-5), // request failed permanently
  API_ETOOMANY(-6), // too many requests for this resource
  API_ERANGE(-7), // resource access out of rage
  API_EEXPIRED(-8), // resource expired
  API_ENOENT(-9), // resource does not exist
  API_ECIRCULAR(-10), // circular linkage
  API_EACCESS(-11), // access denied
  API_EEXIST(-12), // resource already exists
  API_EINCOMPLETE(-13), // request incomplete
  API_EKEY(-14), // cryptographic error
  API_ESID(-15), // bad session ID
  API_EBLOCKED(-16), // resource administratively blocked
  API_EOVERQUOTA(-17), // quote exceeded
  API_ETEMPUNAVAIL(-18), // resource temporarily not available
  API_ETOOMANYCONNECTIONS(-19), // too many connections on this resource
  API_EWRITE(-20), // file could not be written to
  API_EREAD(-21), // file could not be read from
  API_EAPPKEY(-22); // invalid or missing application key

  int code;

  APIError(int code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return "APIError[" + code + ":" + getMessage() + "]";
  }

  public static APIError getError(int code) {
    for (APIError error : values())
      if (error.code == code)
        return error;
    return null;
  }

  public String getMessage() {
    ResourceBundle props = ResourceBundle.getBundle(getClass().getName());
    try {
      return props.getString(name());
    } catch (Exception ex) {
      return "Unknown api error: " + code;
    }
  }

  public String getMessage2() {
    switch (this) {
    case API_OK:
      return "No error";
    case API_EINTERNAL:
      return "Internal error";
    case API_EARGS:
      return "Invalid argument";
    case API_EAGAIN:
      return "Request failed, retrying";
    case API_ERATELIMIT:
      return "Rate limit exceeded";
    case API_EFAILED:
      return "Transfer failed";
    case API_ETOOMANY:
      return "Too many concurrent connections or transfers";
    case API_ERANGE:
      return "Out of range";
    case API_EEXPIRED:
      return "Expired";
    case API_ENOENT:
      return "Not found";
    case API_ECIRCULAR:
      return "Circular linkage detected";
    case API_EACCESS:
      return "Access denied";
    case API_EEXIST:
      return "Already exists";
    case API_EINCOMPLETE:
      return "Incomplete";
    case API_EKEY:
      return "Invalid key/Decryption error";
    case API_ESID:
      return "Bad session ID";
    case API_EBLOCKED:
      return "Blocked";
    case API_EOVERQUOTA:
      return "Over quota";
    case API_ETEMPUNAVAIL:
      return "Temporarily not available";
    case API_ETOOMANYCONNECTIONS:
      return "Connection overflow";
    case API_EWRITE:
      return "Write error";
    case API_EREAD:
      return "Read error";
    case API_EAPPKEY:
      return "Invalid application key";
    default:
      return "Unknown error";
    }
  }
}
