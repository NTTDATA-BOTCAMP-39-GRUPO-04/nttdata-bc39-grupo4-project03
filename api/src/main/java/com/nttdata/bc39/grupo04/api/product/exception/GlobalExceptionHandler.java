package com.nttdata.bc39.grupo04.api.product.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.Serializable;

@ControllerAdvice
@JsonSerialize
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6794973856582385362L;

	@JsonSerialize
	@Data
	private class JsonResponse implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8238178998745934628L;

		String date;

		String message;

		String httpStatus;

		int code;

		public JsonResponse(String message, String httpStatus, String date, int code) {
			super();
			this.message = message;
			this.httpStatus = httpStatus;
			this.date = date;
			this.code = code;
		}
	}

	@ExceptionHandler({ ServiceException.class })
	@ResponseBody
	@JsonSerialize
	public ResponseEntity<JsonResponse> handleRuntimeException(ServiceException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
				.body(new JsonResponse(ex.getMessage(), ex.getHttpStatus(), ex.getDate(), ex.getCode()));
	}

}