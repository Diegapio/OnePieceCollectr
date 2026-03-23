package com.jp;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private static final String URL = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:6543/postgres";
    private static final String USER = "postgres.yllqjmkurbaatgagapzd";
    private static final String PASSWORD = "DiegoMeCagoEnTuVieja";

    public static Connection conectar() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}