package core;

import bean.ColumnInfo;
import bean.TableInfo;
import utils.JavaFileUtils;
import utils.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责管理数据库所有的表结构和类结构的关系，并可以根据表结构生成类架构
 */
public class TableContext {
    /**
     * 表名为key，表信息对象为value
     */
    public static Map<String , TableInfo> tables = new HashMap<String,TableInfo>();
    /**
     * 将po的class对象和表信息对象关联起来，便于重用
     */
    public static Map<Class,TableInfo> poClassTableMap = new HashMap<>();

    public TableContext(){}

    //不需要记
    static {
        try {
            //初始化获得表的信息
            Connection con = DBManager.getConn();
            DatabaseMetaData dbmd = con.getMetaData();
            ResultSet tableRet = dbmd.getTables(null,"%","%",new String[]{"TABLE"});
            while(tableRet.next()){
                String tableName = (String) tableRet.getObject("TABLE_NAME");
                TableInfo ti = new TableInfo(tableName,new ArrayList<ColumnInfo>(),new HashMap<String, ColumnInfo>());
                tables.put(tableName,ti);
                ResultSet set = dbmd.getColumns(null,"%",tableName,"%");
                while(set.next()){
                    ColumnInfo ci = new ColumnInfo(set.getString("COLUMN_NAME"),set.getString("TYPE_NAME"),0);
                    ti.getColumns().put(set.getString("COLUMN_NAME"),ci);
                }
                ResultSet set2 = dbmd.getPrimaryKeys(null,"%",tableName);
                while(set2.next()){
                    ColumnInfo ci2 = (ColumnInfo) ti.getColumns().get(set2.getObject("COLUMN_NAME"));
                    ci2.setKeyType(1);
                    ti.getPriKeys().add(ci2);
                }
                if(ti.getPriKeys().size() > 0){ // 取唯一主键，方便使用，如果是联合主键，则为空
                    ti.setOnlyPrikey(ti.getPriKeys().get((0)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //更新类结构
        updateJavaPOFile();
        //加载po包下面的类，便于重用，提高效率
        loadPOTables();
    }

    /**
     * 根据表结构，更新配置的po包下面的java类
     */
    public static void updateJavaPOFile(){
        Map<String,TableInfo> map = TableContext.tables;
        for(TableInfo t : map.values()){
            JavaFileUtils.createJavaPOFile(t,new MysqlTypeConvertor());
        }
    }

    /**
     * 加载po包下的类
     */
    public static void loadPOTables(){
        try {
            for(TableInfo t : tables.values()) {
                Class c = Class.forName(DBManager.getConf().getPoPackage() + "." + StringUtils.firstChar2UpperCase(t.getTname()));
                poClassTableMap.put(c,t);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    /*
    public static void main(String[] args) {
        Map<String,TableInfo> tables = TableContext.tables;
        System.out.println(tables);
    }
     */
}
