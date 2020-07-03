package pool;

import core.DBManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 连接池的类
 */
public class DBConnPool {
    /**
     * 连接池对象
     */
    private List<Connection> pool;
    /**
     * 最大连接数
     */
    private static final int POOL_MAX_SIZE = DBManager.getConf().getPoolMaxSize();
    /**
     * 最小连接数
     */
    private static final int POOL_MIN_SIZE = DBManager.getConf().getPoolMinSize();

    public DBConnPool(){
        initPool();
    }
    /**
     * 初始化连接池，使池中的连接数达到最小值
     */
    public void initPool(){
        if(pool == null){
            pool = new ArrayList<Connection>();
        }
        while(pool.size() < DBConnPool.POOL_MIN_SIZE){
            pool.add(DBManager.CreateConn());
        }
    }

    /**
     * 从连接池中取出一个连接
     * @return 连接池中的一个连接
     */
    public synchronized Connection getConnection(){
        int last = pool.size() - 1;
        Connection conn = pool.get(last);
        pool.remove(last);
        return conn;
    }

    /**
     * 将连接放回池中
     * @param conn 要放回的连接
     */
    public synchronized void close(Connection conn){
        if(pool.size() >= DBConnPool.POOL_MAX_SIZE){
            try {
                if(conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            pool.add(conn);
        }
    }

}
