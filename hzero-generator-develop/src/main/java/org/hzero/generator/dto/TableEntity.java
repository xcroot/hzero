package org.hzero.generator.dto;

import java.util.List;

/**
 * 表数据
 * 
 * @name TableEntity
 * @description
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:19:29
 * @version
 */
public class TableEntity {
	// 表的名称
	private String tableName;
	// 表的备注
	private String comments;
	// 表的主键
	private ColumnEntity pk;
	// 表的列名(不包含主键)
	private List<ColumnEntity> columns;
	// 表的索引
	private List<IndexEntity> indexs;
	// 类名(第一个字母大写)，如：sys_user => SysUser
	private String className;
	// 类名(第一个字母小写)，如：sys_user => sysUser
	private String classname;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public ColumnEntity getPk() {
		return pk;
	}

	public void setPk(ColumnEntity pk) {
		this.pk = pk;
	}

	public List<ColumnEntity> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnEntity> columns) {
		this.columns = columns;
	}

	public List<IndexEntity> getIndexs() {
        return indexs;
    }

    public void setIndexs(List<IndexEntity> indexs) {
        this.indexs = indexs;
    }

    public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}
}
