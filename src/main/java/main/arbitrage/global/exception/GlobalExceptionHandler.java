package main.arbitrage.global.exception;

import java.sql.SQLException;

import main.arbitrage.infrastructure.exchange.binance.priv.rest.exception.BinancePrivateRestException;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.exception.UpbitPrivateRestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 인증 관련 예외
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        log.error("BadCredentialsException: {}", e.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.error("AuthenticationException: {}", e.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
    }

    // 유효성 검사 관련 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getBindingResult().getAllErrors());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation constraints violated");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.error("Binding error: {}", e.getBindingResult().getAllErrors());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request parameters");
    }

    // 데이터베이스 관련 예외
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "Data integrity violation");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("Duplicate key: {}", e.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "Duplicate entry");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity not found: {}", e.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found");
    }

    // 요청 관련 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid argument");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error("Missing header: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Missing required header");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error("Missing parameter: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Missing required parameter");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid parameter type");
    }

    // 파일 업로드 관련 예외
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("File size exceeded: {}", e.getMessage());
        return createErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeded maximum limit");
    }

    // 404 Not Found
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("No handler found: {}", e.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found");
    }

    // SQL 예외
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException e) {
        log.error("SQL error: {}", e.getMessage());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred");
    }

    // 기타 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    @ExceptionHandler(UpbitPrivateRestException.class)
    public ResponseEntity<ErrorResponse> handleUpbitPrivateRestException(UpbitPrivateRestException e) {
        log.error("Upbit Private RestAPI error: ({}) {}", e.getErrorCode(), e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BinancePrivateRestException.class)
    public ResponseEntity<ErrorResponse> handleBinancePrivateRestException(BinancePrivateRestException e) {
        log.error("Binance Private RestAPI error: ({}) {}", e.getErrorCode(), e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse(status, message);
        return new ResponseEntity<>(response, status);
    }
}