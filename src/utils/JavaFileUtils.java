package utils;

import bean.ColumnInfo;
import bean.JavaFieldGetSet;
import bean.TableInfo;
import core.DBManager;
import core.TypeConvertor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 封装了生成java文件（源代码）常用的操作
 */
public class JavaFileUtils {

    /**
     * 根据字段信息生成java属性信息，如：varchar userName-->private String userName; 以及相应的set和get方法源码
     * @param column 字段信息
     * @param convertor 类型转化器
     * @return java属性和set/get方法源码
     */
    public static JavaFieldGetSet createFieldGetSet(ColumnInfo column, TypeConvertor convertor){
        //属性
        JavaFieldGetSet jfgs = new JavaFieldGetSet();
        String JavaFieldType = convertor.databaseType2JavaType(column.getDataType());
        jfgs.setFieldInfo("\tprivate " + JavaFieldType + " " + column.getName() + ";\n");

        //get方法
        StringBuilder getSrc = new StringBuilder();
        getSrc.append("\tpublic " + JavaFieldType + " get" + StringUtils.firstChar2UpperCase(column.getName()) + "(){\n");
        getSrc.append("\t\treturn " + column.getName() + ";\n");
        getSrc.append("\t}\n");
        jfgs.setGetInfo(getSrc.toString());

        //set方法
        StringBuilder setSrc = new StringBuilder();
        setSrc.append("\tpublic void set" + StringUtils.firstChar2UpperCase(column.getName()) + "("
                        + JavaFieldType + " " + column.getName() + "){\n");
        setSrc.append("\t\tthis." + column.getName() + " = " + column.getName() + ";\n");
        setSrc.append("\t}\n");
        jfgs.setSetInfo(setSrc.toString());

        return jfgs;
    }

    /**
     * 根据表信息生成java类的源代码
     * @param tableInfo 表信息
     * @param convertor 数据类型转化器
     * @return java类的源代码
     */
    public static String createJavaSrc(TableInfo tableInfo, TypeConvertor convertor){
        Map<String, ColumnInfo> columns = tableInfo.getColumns();
        List<JavaFieldGetSet> javaFields = new ArrayList<>();
        for(ColumnInfo c : columns.values()){
            javaFields.add(createFieldGetSet(c,convertor));
        }

        StringBuilder src = new StringBuilder();
        //package语句
        src.append("package " + DBManager.getConf().getPoPackage() + ";\n\n");
        //import语句
        src.append("import java.sql.*;\n");
        src.append("import java.util.*;\n\n");
        //生成属性列表
        src.append("public class " + StringUtils.firstChar2UpperCase(tableInfo.getTname()) + " {\n\n");
        for(JavaFieldGetSet jf : javaFields){
            src.append(jf.getFieldInfo());
        }
        src.append("\n\n");
        //生成set方法列表
        for(JavaFieldGetSet jf : javaFields){
            src.append(jf.getSetInfo());
        }
        //生成get方法列表
        for(JavaFieldGetSet jf : javaFields){
            src.append(jf.getGetInfo());
        }
        //结束
        src.append("}");

        //System.out.println(src);
        return src.toString();
    }

    public static void createJavaPOFile(TableInfo tableInfo, TypeConvertor convertor){
        String src = createJavaSrc(tableInfo,convertor);
        String srcPath = DBManager.getConf().getSrcPath() + "/";
        String packagePath = DBManager.getConf().getPoPackage().replaceAll("/.","/");

        File f = new File(srcPath + packagePath);
        //System.out.println(f.getAbsolutePath());
        if(!f.exists()) f.mkdir();

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f.getAbsolutePath()  + "/" + StringUtils.firstChar2UpperCase(tableInfo.getTname()) + ".java"));
            bw.write(src);
            bw.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(bw != null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*
    public static void main(String[] args) {
        Map<String,TableInfo> map = TableContext.tables;
        for(TableInfo t : map.values()){
            JavaFileUtils.createJavaPOFile(t,new MysqlTypeConvertor());
        }

    }

     */

}
