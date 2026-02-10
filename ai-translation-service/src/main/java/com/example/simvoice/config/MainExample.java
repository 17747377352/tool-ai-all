package com.example.simvoice.config;

import java.io.IOException;

public class MainExample {
    public static void main(String[] args) {
        // 示例1：转换 SQL 文件
        String sqliteSqlFile = "/Users/gongxuesong/Desktop/模版/ime.sql";
        String mysqlSqlFile = "/Users/gongxuesong/Desktop/模版/imemysql.sql";

        boolean success = SqliteToMysqlConverter.convertSqlFile(
                sqliteSqlFile, mysqlSqlFile, "utf8mb4", "InnoDB");

        if (success) {
            System.out.println("SQL 文件转换成功！");
        }

        // 示例2：直接从 SQLite 数据库文件转换
        success = SqliteToMysqlConverter.convertSqliteDbToMysql(
                "data.db", "output_mysql.sql", "utf8mb4", "InnoDB");

        // 示例3：批量转换目录
        try {
            SqliteToMysqlConverter.batchConvertDirectory(
                    "./sqlite_dumps", "./mysql_dumps", "utf8mb4", "InnoDB");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}