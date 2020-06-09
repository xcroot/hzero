package org.hzero.generator.export.helper;

import org.hzero.generator.export.helper.entity.Column;
import org.hzero.generator.export.helper.entity.Data;
import org.hzero.generator.export.helper.entity.DataGroup;
import org.hzero.generator.export.helper.entity.ReferenceColumn;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Liquibase Helper
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 9:26
 */
public class LiquibaseHelper {
    private static LiquibaseEngine liquibaseEngine;
    private static List<DataGroup> dataGroupList;

    private LiquibaseHelper() {
    }

    /**
     * 创建引擎对象
     * 追加模式写入文件
     *
     * @param filePath 文件路径，必须是文件
     */
    public static DataGroupBuilder appendToFile(String filePath) {
        liquibaseEngine = LiquibaseEngine.createEngine(filePath, LiquibaseEngineMode.APPEND);
        dataGroupList = new LinkedList<>();
        liquibaseEngine.setDataGroupList(dataGroupList);
        return new DataGroupBuilder();
    }

    /**
     * 创建引擎对象
     * 新建模式创建新文件
     *
     * @param filePath 文件路径，必须是文件
     */
    public static DataGroupBuilder createNewFile(String filePath) {
        liquibaseEngine = LiquibaseEngine.createEngine(filePath, LiquibaseEngineMode.CREATE);
        dataGroupList = new LinkedList<>();
        liquibaseEngine.setDataGroupList(dataGroupList);
        return new DataGroupBuilder();
    }

    public static void generate() {
        liquibaseEngine.generate();
    }

    /**
     * 创建引擎对象
     * 新建模式创建新文件
     *
     * @param filePath 文件路径，必须是文件
     */
    public static DataGroupBuilder overrideFile(String filePath) {
        liquibaseEngine = LiquibaseEngine.createEngine(filePath, LiquibaseEngineMode.OVERRIDE);
        dataGroupList = new LinkedList<>();
        liquibaseEngine.setDataGroupList(dataGroupList);
        return new DataGroupBuilder();
    }

    public static class DataGroupBuilder {
        private DataGroup dataGroup;
        private List<Data> dataList;
        private DataBuilder dataBuilder;

        private DataGroupBuilder() {
            dataGroup = new DataGroup();
            dataList = new LinkedList<>();
            dataGroup.setDataList(dataList);
            dataGroupList.add(dataGroup);
        }

        public DataGroupBuilder sheetName(String sheetName) {
            dataGroup.setSheetName(sheetName);
            return this;
        }

        public DataBuilder data() {
            dataBuilder = new DataBuilder(this);
            dataList.add(dataBuilder.data);
            return dataBuilder;
        }

        public static class DataBuilder {
            private Data data;
            private List<Column> columnList;
            private DataGroupBuilder dataGroupBuilder;
            private ColumnBuilder columnBuilder;

            private DataBuilder(DataGroupBuilder dataGroupBuilder) {
                data = new Data();
                columnList = new LinkedList<>();
                data.setColumnList(columnList);
                this.dataGroupBuilder = dataGroupBuilder;
            }

            public DataBuilder tableName(String tableName) {
                data.setTableName(tableName);
                return this;
            }

            public DataBuilder where(String where){
                data.setWhere(where);
                return this;
            }

            public DataBuilder creationDate(LocalDate creationDate) {
                data.setCreationDate(creationDate);
                return this;
            }

            public DataBuilder author(String author) {
                data.setAuthor(author);
                return this;
            }

            public DataBuilder description(String description) {
                data.setDescription(description);
                return this;
            }

            public ColumnBuilder column() {
                columnBuilder = new ColumnBuilder(dataGroupBuilder, this);
                columnList.add(columnBuilder.column);
                return columnBuilder;
            }

            public static class ColumnBuilder {
                private Column column;
                private DataGroupBuilder dataGroupBuilder;
                private DataBuilder dataBuilder;

                private ColumnBuilder(DataGroupBuilder dataGroupBuilder, DataBuilder dataBuilder) {
                    column = new Column();
                    this.dataGroupBuilder = dataGroupBuilder;
                    this.dataBuilder = dataBuilder;
                }

                public ColumnBuilder columnName(String columnName) {
                    column.setColumnName(columnName);
                    return this;
                }

                public ColumnBuilder autoGenerate() {
                    column.setAutoGenerate(true);
                    return this;
                }

                public ColumnBuilder unique() {
                    column.setUnique(true);
                    return this;
                }

                public ColumnBuilder type(String type) {
                    column.setType(type);
                    return this;
                }

                public ColumnBuilder pkName(String pkName){
                    column.setPkName(pkName);
                    return this;
                }

                public ColumnBuilder cited() {
                    column.setCited(true);
                    return this;
                }

                public ColumnBuilder lang(String lang, String... other) {
                    Assert.notNull(lang, "语言不能为 [null]！");
                    List<String> langList = new LinkedList<>();
                    langList.add(lang);
                    if (other != null && other.length > 0) {
                        langList.addAll(Arrays.asList(other));
                    }
                    column.setLang(langList);
                    return this;
                }

                public ColumnBuilder reference(String sheetName, String tableName, String columnName) {
                    column.setReference(new ReferenceColumn(sheetName, tableName, columnName));
                    return this;
                }

                public ColumnBuilder reference(String tableName, String columnName) {
                    column.setReference(new ReferenceColumn(tableName, columnName));
                    return this;
                }

                public DataGroupBuilder nextSheet(){
                    return new DataGroupBuilder();
                }

                public DataBuilder nextData() {
                    DataBuilder nextDataBuilder = new DataBuilder(dataGroupBuilder);
                    dataGroupBuilder.dataList.add(nextDataBuilder.data);
                    return nextDataBuilder;
                }

                public ColumnBuilder nextColumn(){
                    ColumnBuilder nextColumnBuilder = new ColumnBuilder(dataGroupBuilder, dataBuilder);
                    dataBuilder.columnList.add(nextColumnBuilder.column);
                    return nextColumnBuilder;
                }

                public void generate() {
                    liquibaseEngine.generate();
                }

            }
        }
    }
}
