package com.kyn.dao;

import com.kyn.po.User;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserInfoDaoImpl implements UserInfoDao{

    //数据源对象
    private DataSource dataSource;

    //setter方法注入
    public void setDataSource(DataSource dataSource){
        this.dataSource=dataSource;
    }
    // 初始化方法
    public void init() {
        System.out.println("初始化方法被调用");
    }

    @Override
    public List<User> queryUserList(Map<String, Object> map) {
        Connection conn=null;
        PreparedStatement psmt=null;
        ResultSet rs=null;
        try{
            //通过数据源获取数据库连接
            conn=dataSource.getConnection();
            //定义sql,?表示占位符
            String sql="select * from user where username= ? ";
            //获取预处理对象
            psmt=conn.prepareStatement(sql);
            //向sql语句中传值，第一个参数是sql语句中参数的序号（从1开始），第二个参数是要传递的值
            psmt.setObject(1,map.get("username"));
            //执行sql语句，获取结果集
            rs = psmt.executeQuery();
            //获取结果集的元数据
            ResultSetMetaData rsmd=rs.getMetaData();
            //获取结果集中的列的数量
            int count=rsmd.getColumnCount();

            User user =null;
            Class<?> clazz= User.class;
            List<User> result=new ArrayList<>();
            while(rs.next()){
                user =(User)clazz.newInstance();
                //遍历所有的列
                for (int i=0;i<count;i++) {
                    //获取每一列的名称(下标从1开始）
                    String columnName=rsmd.getColumnName(i+1);
                    //通过反射获取指定属性名的Field对象（保证列名和属性名一致）
                    Field field=clazz.getDeclaredField(columnName);
                    field.setAccessible(true);
                    //给私有属性赋值（下标从开始）
                    field.set(user,rs.getObject(i+1));
                }
                result.add(user);
            }
            return result;

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            // 释放资源
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psmt != null) {
                try {
                    psmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
        return null;
    }
}
