package com.usmarinec.ledger.responses;

import com.usmarinec.ledger.LedgerDocument;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessFailureResponse<T extends LedgerDocument>
    implements SuccessFailureResponseInterface {

  /**
   * Creates a response for a success.
   *
   * @return SuccessFailureResponse
   */
  public static <T> SuccessFailureResponse<T> success() {
    return SuccessFailureResponse.<T>builder()
        .success(true)
        .message(StringUtils.EMPTY)
        .httpStatus(null)
        .build();
  }

  /**
   * Creates a response for a success with message.
   *
   * @param message success message
   * @return SuccessFailureResponse
   */
  public static <T> SuccessFailureResponse<T> success(String message) {
    return SuccessFailureResponse.<T>builder()
        .success(true)
        .message(message)
        .httpStatus(null)
        .build();
  }

  /**
   * Creates a response for a success with message and error code.
   *
   * @param message success message
   * @param errorCode type of error
   * @return SuccessFailureResponse
   */
  public static <T> SuccessFailureResponse<T> success(String message, String errorCode) {
    return SuccessFailureResponse.<T>builder()
        .success(true)
        .message(message)
        .httpStatus(errorCode)
        .build();
  }

  /**
   * Creates a response for a success with message, errorCode, and T item.
   *
   * @param message success message
   * @param errorCode type of error
   * @param item item returned by controller
   * @return SuccessFailureResponse
   */
  public static <T> SuccessFailureResponse<T> success(String message, String errorCode, T item) {
    List<T> items = new ArrayList<>();
    items.add(item);
    return SuccessFailureResponse.<T>builder()
        .success(true)
        .message(message)
        .items(items)
        .httpStatus(errorCode)
        .build();
  }

  /**
   * Creates a response for a success with message, errorCode, and ListT items.
   *
   * @param message success message
   * @param errorCode type of error
   * @param items items returned by controller
   * @return SuccessFailureResponse
   */
  public static <T> SuccessFailureResponse<T> success(
      String message, String errorCode, List<T> items) {
    return SuccessFailureResponse.<T>builder()
        .success(true)
        .message(message)
        .items(items)
        .httpStatus(errorCode)
        .build();
  }

  /**
   * Creates a response for a failure with message.
   *
   * @param message failure message
   * @return SuccessFailureResponse
   */
  public static <T> SuccessFailureResponse<T> failure(String message) {
    return SuccessFailureResponse.<T>builder()
        .success(false)
        .message(message)
        .httpStatus(null)
        .build();
  }

  /**
   * Creates a response for a failure with message and error code.
   *
   * @param message failure message
   * @param errorCode type of error
   * @return SuccessFailureResponse
   */
  public static <T> SuccessFailureResponse<T> failure(String message, String errorCode) {
    return SuccessFailureResponse.<T>builder()
        .success(false)
        .message(message)
        .httpStatus(errorCode)
        .build();
  }

  private boolean success;
  private String message;
  private List<T> items;
  private String httpStattus;

  @Override
  public boolean getSuccess() {
    return this.success;
  }
}
