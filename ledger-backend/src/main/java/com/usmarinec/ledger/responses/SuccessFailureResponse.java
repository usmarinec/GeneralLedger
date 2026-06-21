package com.usmarinec.ledger.responses;

import com.usmarinec.ledger.dto.Response;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Schema(
    name = "SuccessFailureResponse",
    description = "Standard API response wrapper used by ledger endpoints.")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessFailureResponse<ResponseT extends Response>
    implements SuccessFailureResponseInterface {

  /**
   * Creates a response for a success.
   *
   * @return SuccessFailureResponse
   */
  public static <ResponseT extends Response> SuccessFailureResponse<ResponseT> success() {
    return SuccessFailureResponse.<ResponseT>builder()
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
  public static <ResponseT extends Response> SuccessFailureResponse<ResponseT> success(
      String message) {
    return SuccessFailureResponse.<ResponseT>builder()
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
  public static <ResponseT extends Response> SuccessFailureResponse<ResponseT> success(
      String message, String errorCode) {
    return SuccessFailureResponse.<ResponseT>builder()
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
  public static <ResponseT extends Response> SuccessFailureResponse<ResponseT> success(
      String message, String errorCode, ResponseT item) {
    List<ResponseT> items = new ArrayList<>();
    items.add(item);
    return SuccessFailureResponse.<ResponseT>builder()
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
  public static <ResponseT extends Response> SuccessFailureResponse<ResponseT> success(
      String message, String errorCode, List<ResponseT> items) {
    return SuccessFailureResponse.<ResponseT>builder()
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
  public static <ResponseT extends Response> SuccessFailureResponse<ResponseT> failure(
      String message) {
    return SuccessFailureResponse.<ResponseT>builder()
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
  public static <ResponseT extends Response> SuccessFailureResponse<ResponseT> failure(
      String message, String errorCode) {
    return SuccessFailureResponse.<ResponseT>builder()
        .success(false)
        .message(message)
        .httpStatus(errorCode)
        .build();
  }

  @Schema(description = "Whether the request completed successfully.", example = "true")
  private boolean success;

  @Schema(description = "Human-readable response message.", example = "Record created")
  private String message;

  @Schema(description = "Response items returned by the endpoint.")
  private List<ResponseT> items;

  @Schema(description = "HTTP status reason phrase.", example = "Created")
  private String httpStatus;

  @Override
  public boolean getSuccess() {
    return this.success;
  }
}
