package org.hzero.generator.util;

/**
 * 自定义异常
 * 
 * @name HzeroException
 * @description
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:23:29
 * @version
 */
public class GenException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	private String msg;
	private int code = 500;

	public GenException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public GenException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public GenException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public GenException(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
