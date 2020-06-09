package org.hzero.generator.entity;

/**
 * @Description 服务映射
 * @Date 2019/12/17 20:41
 * @Author wanshun.zhang@hand-china.com
 */
public class Mapping {

    private String name;
    private String filename;
    private String schema;
    private String username;
    private String password;
    private String bucket;
    private String description;

    public Mapping() {
    }

    public Mapping(String name, String filename, String schema, String username, String password, String description) {
        this.name = name;
        this.filename = filename;
        this.schema = schema;
        this.username = username;
        this.password = password;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getBucket() {
        return bucket;
    }

    public Mapping setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", schema='" + schema + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
