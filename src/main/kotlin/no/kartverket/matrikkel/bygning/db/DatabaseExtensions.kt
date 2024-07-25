package no.kartverket.matrikkel.bygning.db

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource

inline fun <T> DataSource.connection(block: (Connection) -> T) = connection.use(block)

inline fun <T> Connection.createStatement(block: (Statement) -> T) = createStatement().use(block)
inline fun <T> Statement.executeQuery(sql: String, block: (ResultSet) -> T) = executeQuery(sql).use(block)

inline fun <T> Connection.prepareStatement(sql: String, block: (PreparedStatement) -> T) = prepareStatement(sql).use(block)
inline fun <T> PreparedStatement.executeQuery(block: (ResultSet) -> T) = executeQuery().use(block)

fun <T> DataSource.executeAndMapStatement(
    sql: String, resultMapper: (ResultSet) -> T
): List<T> {
    return this.connection { connection ->
        connection.createStatement { statement ->
            statement.executeQuery(sql) { resultSet ->
                generateSequence {
                    if (resultSet.next()) resultMapper(resultSet) else null
                }.toList()
            }
        }
    }
}

fun <T> DataSource.executeAndMapPreparedStatement(
    sql: String, vararg setters: (PreparedStatement) -> Unit, resultMapper: (ResultSet) -> T
): List<T> {
    return this.connection { connection ->
        connection.prepareStatement(sql) { preparedStatement ->
            setters.forEach { it(preparedStatement) }
            preparedStatement.executeQuery() { resultSet ->
                generateSequence {
                    if (resultSet.next()) resultMapper(resultSet) else null
                }.toList()
            }
        }
    }
}

