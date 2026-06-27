export interface SuccessFailureResponse<T> {
  success: boolean;
  message: string;
  items: T[];
  httpStatus: string;
}
