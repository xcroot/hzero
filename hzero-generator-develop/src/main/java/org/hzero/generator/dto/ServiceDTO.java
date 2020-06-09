package org.hzero.generator.dto;

import java.util.List;

/**
 * 脚本类型实体
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/01 14:52
 */
public class ServiceDTO {

    private String id;
    private String title;
    private String field;
    private Integer order;
    private Boolean spread;
    private Boolean disabled;
    private List<Children> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getSpread() {
        return spread;
    }

    public void setSpread(Boolean spread) {
        this.spread = spread;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public List<Children> getChildren() {
        return children;
    }

    public void setChildren(List<Children> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "ServiceDTO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", field='" + field + '\'' +
                ", order=" + order +
                ", spread=" + spread +
                ", disabled=" + disabled +
                ", children=" + children +
                '}';
    }

    public static class Children{
        private String id;
        private String title;
        private String field;
        private Boolean disabled;
        private List<Children> children;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public List<Children> getChildren() {
            return children;
        }

        public void setChildren(List<Children> children) {
            this.children = children;
        }
    }
}
