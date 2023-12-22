package com.audi.test.myself;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * User : LinShiYue
 * Date : 2023-12-07 13:45:01
 * Description :
 */
public class PhoenixExample {
    public static void main(String[] args) {
        String url = "jdbc:phoenix:hadoop102:2181";
        try (Connection conn = DriverManager.getConnection(url)) {
            // 在这里执行 Phoenix 查询等操作
            System.out.println(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
