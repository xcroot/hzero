package org.hzero.generator.scan.domain;


/**
 * description
 *
 * @author fanghan.liu 2020/02/27 11:01
 */
public class Permission {

    private Long id;
    private String code;
    private String path;
    private String method;
    private String level;
    private String description;
    private String action;
    private String resource;
    private Boolean publicAccess;
    private Boolean loginAccess;
    private Boolean signAccess;
    private Boolean within;
    private String serviceName;
    private String tag;

    public Long getId() {
        return id;
    }

    public Permission setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Permission setCode(String code) {
        this.code = code;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Permission setPath(String path) {
        this.path = path;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public Permission setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public Permission setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Permission setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAction() {
        return action;
    }

    public Permission setAction(String action) {
        this.action = action;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public Permission setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public Boolean getPublicAccess() {
        return publicAccess;
    }

    public Permission setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
        return this;
    }

    public Boolean getLoginAccess() {
        return loginAccess;
    }

    public Permission setLoginAccess(Boolean loginAccess) {
        this.loginAccess = loginAccess;
        return this;
    }

    public Boolean getSignAccess() {
        return signAccess;
    }

    public Permission setSignAccess(Boolean signAccess) {
        this.signAccess = signAccess;
        return this;
    }

    public Boolean getWithin() {
        return within;
    }

    public Permission setWithin(Boolean within) {
        this.within = within;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Permission setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public Permission setTag(String tag) {
        this.tag = tag;
        return this;
    }
}
