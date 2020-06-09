package org.hzero.generator.scan.domain.vo;

import java.util.List;

/**
 * 服务路由视图
 *
 * @author fanghan.liu 2020/02/13 14:28
 */
public class ServiceRouteVO {

    private String id;
    private String title;
    private String field;
    private Boolean disabled;
    private List<ServiceRouteVO> children;

    public ServiceRouteVO() {
    }

    public ServiceRouteVO(String title) {
        this.title = title;
    }

    public ServiceRouteVO(String title, List<ServiceRouteVO> children) {
        this.title = title;
        this.children = children;
    }

    public ServiceRouteVO(String id, String title, String field, List<ServiceRouteVO> children) {
        this.id = id;
        this.title = title;
        this.field = field;
        this.children = children;
    }

    public String getTitle() {
        return title;
    }

    public ServiceRouteVO setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<ServiceRouteVO> getChildren() {
        return children;
    }

    public ServiceRouteVO setChildren(List<ServiceRouteVO> children) {
        this.children = children;
        return this;
    }

    public String getId() {
        return id;
    }

    public ServiceRouteVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getField() {
        return field;
    }

    public ServiceRouteVO setField(String field) {
        this.field = field;
        return this;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public ServiceRouteVO setDisabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    public String toString() {
        return "ServiceRouteVO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", field='" + field + '\'' +
                ", disabled=" + disabled +
                ", children=" + children +
                '}';
    }
}
