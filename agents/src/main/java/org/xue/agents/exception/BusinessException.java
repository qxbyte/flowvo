package org.xue.agents.exception;

/**
 * 业务逻辑异常
 * 用于处理业务验证失败等情况，不应该记录为ERROR级别
 */
public class BusinessException extends RuntimeException {
    
    private final int httpStatus;
    
    public BusinessException(String message) {
        super(message);
        this.httpStatus = 400; // 默认400错误
    }
    
    public BusinessException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 400;
    }
    
    public BusinessException(String message, Throwable cause, int httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
} 