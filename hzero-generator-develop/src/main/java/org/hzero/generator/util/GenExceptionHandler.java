package org.hzero.generator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器
 * 
 * @name GenExceptionHandler
 * @description
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:23:52
 * @version
 */
@RestControllerAdvice
public class GenExceptionHandler {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(GenException.class)
	public Result handleRRException(GenException e) {
		Result r = new Result();
		r.put("code", e.getCode());
		r.put("msg", e.getMessage());
		return r;
	}

	@ExceptionHandler(Exception.class)
	public Result handleException(Exception e) {
		logger.error(e.getMessage(), e);
		return Result.error();
	}
}
