package com.nttdata.bc39.grupo04.credit.exception;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonSerialize
public class ServiceException extends RuntimeException implements Serializable {

	private static final long serialVersionUID = -372398079485004679L;
	private String message;
	private String httpStatus;
	private String date;
	private int code;
	
	
	public ServiceException(String message, String httpStatus, String date, int code) {
		super();
		this.message = message;
		this.httpStatus = httpStatus;
		this.date = date;
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(String httpStatus) {
		this.httpStatus = httpStatus;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}	
	
	

}
