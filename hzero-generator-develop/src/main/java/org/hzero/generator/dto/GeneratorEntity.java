package org.hzero.generator.dto;

/**
 * 代码生成信息
 * 
 * @name GeneratorEntity
 * @description
 * @author xianzhi.chen@hand-china.com 2018年3月8日下午10:28:51
 * @version
 */
public class GeneratorEntity {

	private String tablePrefix;

	private String pkg;

	private String author;
	
	private String level;

	private String[] tableNames;

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public String getPkg() {
		return pkg;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String[] getTableNames() {
		return tableNames;
	}

	public void setTableNames(String[] tableNames) {
		this.tableNames = tableNames;
	}

}
