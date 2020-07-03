package core;

import bean.Configuration;
import pool.DBConnPool;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * 根据配置信息，维持连接对象的管理（增加连接池功能）
 */
public class DBManager {
    /**
     * 配置信息
     */
    private static Configuration conf;
    /**
     * 连接池
     */
    private static DBConnPool pool;

    /**
     * 加载配置信息
     */
    static{ //静态代码块
        Properties pros = new Properties();
        try {
            pros.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        conf = new Configuration();
        conf.setDriver(pros.getProperty("driver"));
        conf.setPoPackage(pros.getProperty("poPackage"));
        conf.setPwd(pros.getProperty("pwd"));
        conf.setSrcPath(pros.getProperty("srcPath"));
        conf.setUrl(pros.getProperty("url"));
        conf.setUser(pros.getProperty("user"));
        conf.setUsingDB(pros.getProperty("usingDB"));
        conf.setQueryClass(pros.getProperty("queryClass"));
        conf.setPoolMinSize(Integer.parseInt(pros.getProperty("poolMinSize")));
        conf.setPoolMaxSize(Integer.parseInt(pros.getProperty("poolMaxSize")));

        //加载TableContext
        System.out.println(TableContext.class);
    }

    /**
     * 创建Connection对象
     * @return Connection 对象
     */
    public static Connection CreateConn(){
        try {
            //目前直接建立连接，后期添加连接池处理，提高效率
            Class.forName(conf.getDriver());
            return DriverManager.getConnection(conf.getUrl(), conf.getUser(),conf.getPwd());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得Connection对象
     * @return Connection 对象
     */
    public static Connection getConn(){
        if(pool == null) pool = new DBConnPool();
        return pool.getConnection();
    }

    /**
     * 关闭传入的ResultSet,Statement,Connection对象
     * @param rs
     * @param stmt
     * @param conn
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn){
        if(rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(stmt != null){
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        pool.close(conn);
    }

    /**
     * 关闭传入的Statement,Connection对象
     * @param stmt
     * @param conn
     */
    public static void close(Statement stmt,Connection conn){
        if(stmt != null){
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        pool.close(conn);
    }

    /**
     * 关闭传入的Connection对象
     * @param conn
     */
    public static void close(Connection conn){
        pool.close(conn);
    }

    /**
     * 返回Configuration信息
     * @return
     */
    public static Configuration getConf(){
        return conf;
    }
}
