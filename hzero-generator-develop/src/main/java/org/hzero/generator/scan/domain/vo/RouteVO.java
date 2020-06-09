package org.hzero.generator.scan.domain.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 路由视图
 *
 * @author fanghan.liu 2020/02/13 14:31
 */
public class RouteVO {

    private String path;
    private String authorized;
    private String component;
    private String key;
    private List<RouteVO> components = new ArrayList<>();
    private String models;

    public RouteVO() {
    }

    public RouteVO(String path) {
        this.path = path;
    }

    public RouteVO(String path, List<RouteVO> components) {
        this.path = path;
        this.components = components;
    }

    public String getPath() {
        return path;
    }

    public RouteVO setPath(String path) {
        this.path = path;
        return this;
    }

    public String getAuthorized() {
        return authorized;
    }

    public RouteVO setAuthorized(String authorized) {
        this.authorized = authorized;
        return this;
    }

    public String getComponent() {
        return component;
    }

    public RouteVO setComponent(String component) {
        this.component = component;
        return this;
    }

    public String getKey() {
        return key;
    }

    public RouteVO setKey(String key) {
        this.key = key;
        return this;
    }

    public List<RouteVO> getComponents() {
        return components;
    }

    public RouteVO setComponents(List<RouteVO> components) {
        this.components = components;
        return this;
    }

    public String getModels() {
        return models;
    }

    public RouteVO setModels(String models) {
        this.models = models;
        return this;
    }

    @Override
    public String toString() {
        return "RouteVO{" +
                "path='" + path + '\'' +
                ", authorized='" + authorized + '\'' +
                ", component='" + component + '\'' +
                ", key='" + key + '\'' +
                ", components=" + components +
                ", models='" + models + '\'' +
                '}';
    }
}
