package com.example.simvoice.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 到 MySQL SQL 转换工具
 * 兼容 JDK 1.8
 */
public class SqliteToMysqlConverter {

    /**
     * 转换 SQLite 的 SQL 文件为 MySQL 兼容格式
     * @param sqliteFilePath SQLite 导出的 SQL 文件路径
     * @param mysqlFilePath 转换后的 MySQL SQL 文件路径
     * @param charset 字符集，默认 utf8mb4
     * @param engine 存储引擎，默认 InnoDB
     * @return 转换是否成功
     */
    public static boolean convertSqlFile(String sqliteFilePath, String mysqlFilePath,
                                         String charset, String engine) {
        if (charset == null || charset.isEmpty()) {
            charset = "utf8mb4";
        }
        if (engine == null || engine.isEmpty()) {
            engine = "InnoDB";
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(sqliteFilePath), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(mysqlFilePath), StandardCharsets.UTF_8))) {

            String line;
            StringBuilder createTableBuffer = null;
            boolean inCreateTable = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过 SQLite 特定的语句
                if (line.startsWith("PRAGMA") || line.startsWith("BEGIN TRANSACTION;")) {
                    continue;
                }

                // 处理 CREATE TABLE 语句（可能跨多行）
                if (line.startsWith("CREATE TABLE") || inCreateTable) {
                    if (!inCreateTable) {
                        createTableBuffer = new StringBuilder();
                        inCreateTable = true;
                    }

                    createTableBuffer.append(line).append(" ");

                    // 检查 CREATE TABLE 是否结束
                    if (line.endsWith(";")) {
                        String createTableSql = processCreateTable(
                                createTableBuffer.toString(), charset, engine);
                        writer.write(createTableSql);
                        writer.newLine();

                        createTableBuffer = null;
                        inCreateTable = false;
                    }
                    continue;
                }

                // 处理 INSERT 语句
                if (line.startsWith("INSERT INTO")) {
                    line = processInsertStatement(line);
                    writer.write(line);
                    writer.newLine();
                    continue;
                }

                // 处理其他语句
                if (!line.isEmpty()) {
                    line = processOtherStatements(line);
                    writer.write(line);
                    writer.newLine();
                }
            }

            return true;

        } catch (IOException e) {
            System.err.println("转换文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 处理 CREATE TABLE 语句
     */
    private static String processCreateTable(String sql, String charset, String engine) {
        // 1. 替换双引号为反引号
        sql = sql.replace("\"", "`");

        // 2. 处理数据类型
        sql = fixDataType(sql);

        // 3. 添加存储引擎和字符集（如果还没有）
        if (!sql.contains("ENGINE=")) {
            sql = sql.replaceFirst(";$",
                    String.format(") ENGINE=%s DEFAULT CHARSET=%s;", engine, charset));
        }

        // 4. 格式化换行
        sql = sql.replace(" (", " (\n  ").replace(", ", ",\n  ");

        return sql;
    }

    /**
     * 修复数据类型定义
     */
    private static String fixDataType(String sql) {
        // 匹配各种数据类型模式
        Pattern pattern = Pattern.compile(
                "`(\\w+)`\\s+(\\w+)(?:\\([^)]*\\))?(\\s+NOT NULL)?(\\s+DEFAULT\\s+[^,)]+)?(\\s+PRIMARY KEY)?",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(sql);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String columnName = matcher.group(1);
            String dataType = matcher.group(2).toUpperCase();
            String notNull = matcher.group(3) != null ? matcher.group(3) : "";
            String defaultValue = matcher.group(4) != null ? matcher.group(4) : "";
            String primaryKey = matcher.group(5) != null ? matcher.group(5) : "";

            // 转换数据类型
            String mysqlDataType = convertDataType(dataType);

            // 重建列定义
            String replacement = String.format("`%s` %s%s%s%s",
                    columnName, mysqlDataType, notNull, defaultValue, primaryKey);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * SQLite 数据类型转 MySQL 数据类型
     */
    private static String convertDataType(String sqliteType) {
        sqliteType = sqliteType.toUpperCase();

        switch (sqliteType) {
            case "INTEGER":
                return "INT";
            case "VARCHAR":
                return "VARCHAR(255)";
            case "TEXT":
                return "TEXT";
            case "REAL":
                return "DOUBLE";
            case "NUMERIC":
                return "DECIMAL(10,2)";
            case "BLOB":
                return "BLOB";
            case "DATETIME":
                return "DATETIME";
            case "BOOLEAN":
                return "TINYINT(1)";
            default:
                // 如果已经有括号（如 VARCHAR(100)），保持不变
                if (sqliteType.contains("(")) {
                    return sqliteType;
                }
                return sqliteType;
        }
    }

    /**
     * 处理 INSERT 语句
     */
    private static String processInsertStatement(String sql) {
        // 1. 替换双引号为反引号
        sql = sql.replace("\"", "`");

        // 2. 处理布尔值（SQLite 用 0/1，MySQL 可用 TRUE/FALSE）
        sql = sql.replace(" 0,", " FALSE,");
        sql = sql.replace(" 1,", " TRUE,");
        sql = sql.replace(" 0)", " FALSE)");
        sql = sql.replace(" 1)", " TRUE)");

        // 3. 处理单引号转义（如果需要）
        sql = sql.replace("''", "'");

        return sql;
    }

    /**
     * 处理其他语句
     */
    private static String processOtherStatements(String sql) {
        // 替换关键字
        sql = sql.replace("AUTOINCREMENT", "AUTO_INCREMENT");
        sql = sql.replace("BEGIN TRANSACTION", "START TRANSACTION");
        sql = sql.replace("\"", "`");

        return sql;
    }

    /**
     * 批量转换整个目录的 SQL 文件
     */
    public static void batchConvertDirectory(String sourceDir, String targetDir,
                                             String charset, String engine) throws IOException {
        File dir = new File(sourceDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("源目录不存在: " + sourceDir);
        }

        File target = new File(targetDir);
        if (!target.exists()) {
            target.mkdirs();
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".sql"));
        if (files != null) {
            for (File file : files) {
                String targetPath = targetDir + File.separator +
                        file.getName().replace(".sql", "_mysql.sql");
                boolean success = convertSqlFile(file.getAbsolutePath(), targetPath, charset, engine);
                if (success) {
                    System.out.println("转换成功: " + file.getName() + " -> " +
                            new File(targetPath).getName());
                }
            }
        }
    }

    /**
     * 直接从 SQLite 数据库文件导出并转换
     */
    public static boolean convertSqliteDbToMysql(String sqliteDbPath, String mysqlSqlPath,
                                                 String charset, String engine) {
        try {
            // 先导出 SQLite 数据库
            ProcessBuilder pb = new ProcessBuilder("sqlite3", sqliteDbPath, ".dump");
            Process process = pb.start();

            File tempFile = File.createTempFile("sqlite_export", ".sql");
            tempFile.deleteOnExit();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }

                process.waitFor();
            }

            // 转换导出的 SQL 文件
            return convertSqlFile(tempFile.getAbsolutePath(), mysqlSqlPath, charset, engine);

        } catch (Exception e) {
            System.err.println("导出 SQLite 数据库失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取 SQL 文件中的表结构信息
     */
    public static List<String> extractTableInfo(String sqlFilePath) throws IOException {
        List<String> tables = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(sqlFilePath), StandardCharsets.UTF_8))) {

            String line;
            StringBuilder currentTable = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("CREATE TABLE")) {
                    if (currentTable != null) {
                        tables.add(currentTable.toString());
                    }
                    currentTable = new StringBuilder(line).append("\n");
                } else if (currentTable != null) {
                    currentTable.append(line).append("\n");
                    if (line.endsWith(";")) {
                        tables.add(currentTable.toString());
                        currentTable = null;
                    }
                }
            }
        }

        return tables;
    }
}