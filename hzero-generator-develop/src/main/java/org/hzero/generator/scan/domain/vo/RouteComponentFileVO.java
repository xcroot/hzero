package org.hzero.generator.scan.domain.vo;

import java.io.File;

/**
 * 路由文件VO
 *
 * @author allen.liu
 * @date 2019/7/30
 */
public class RouteComponentFileVO {
    private String pageRoute;
    private File componentFile;

    public RouteComponentFileVO(String pageRoute, File componentFile) {
        this.pageRoute = pageRoute;
        this.componentFile = componentFile;
    }

    public String getPageRoute() {
        return pageRoute;
    }

    public RouteComponentFileVO setPageRoute(String pageRoute) {
        this.pageRoute = pageRoute;
        return this;
    }

    public File getComponentFile() {
        return componentFile;
    }

    public RouteComponentFileVO setComponentFile(File componentFile) {
        this.componentFile = componentFile;
        return this;
    }
}
