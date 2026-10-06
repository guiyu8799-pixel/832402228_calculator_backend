package com.fzu.calculator.dto;

/**
 * Response of the delete operation.
 */
public class ApiMessageResponse {

    private final boolean success = true;
    private final String message;
    private final Long deletedId;

    public ApiMessageResponse(String message, Long deletedId) {
        this.message = message;
        this.deletedId = deletedId;
    }

    public static ApiMessageResponse deleted(Long id) {
        return new ApiMessageResponse("历史记录 " + id + " 已删除", id);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getDeletedId() {
        return deletedId;
    }
}
