package org.hzero.generator.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 查询参数
 * 
 * @name Query
 * @description
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:22:49
 * @version
 */
public class Query extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	// 当前页码
	private int page;
	// 每页条数
	private int limit;

	public Query(Map<String, Object> params) {
		this.putAll(params);

		// 分页参数
		this.page = Integer.parseInt(params.get("page").toString());
		this.limit = Integer.parseInt(params.get("limit").toString());
		this.put("offset", (page - 1) * limit);
		this.put("page", page);
		this.put("limit", limit);
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
