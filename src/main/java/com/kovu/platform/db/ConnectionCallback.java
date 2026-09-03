package com.kovu.platform.db;

import java.sql.Connection;
import java.sql.SQLException;

/** El trabajo que se hace con una conexión JDBC prestada del pool. */
@FunctionalInterface
public interface ConnectionCallback<T> {
    T doWith(Connection conn) throws SQLException;
}
