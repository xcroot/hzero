package org.hzero.generator.scan.domain;

/**
 * 多语言实体
 *
 * @author fanghan.liu 2020/03/13 13:39
 */
public class Prompt {

    private Long promptId;
    private Long tenantId;
    private String promptKey;
    private String promptCode;
    private String description;
    private String lang;

    public Long getPromptId() {
        return promptId;
    }

    public Prompt setPromptId(Long promptId) {
        this.promptId = promptId;
        return this;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Prompt setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getPromptKey() {
        return promptKey;
    }

    public Prompt setPromptKey(String promptKey) {
        this.promptKey = promptKey;
        return this;
    }

    public String getPromptCode() {
        return promptCode;
    }

    public Prompt setPromptCode(String promptCode) {
        this.promptCode = promptCode;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Prompt setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public Prompt setLang(String lang) {
        this.lang = lang;
        return this;
    }

    @Override
    public String toString() {
        return "Prompt{" +
                "promptId=" + promptId +
                ", tenantId=" + tenantId +
                ", promptKey='" + promptKey + '\'' +
                ", promptCode='" + promptCode + '\'' +
                ", description='" + description + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }
}
