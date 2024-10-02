package no.kartverket.matrikkel.bygning.infrastructure.database

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

inline fun <T> DataSource.connection(block: (Connection) -> T) = connection.use(block)

inline fun <T> Connection.createStatement(block: (Statement) -> T) = createStatement().use(block)
inline fun <T> Statement.executeQuery(sql: String, block: (ResultSet) -> T) = executeQuery(sql).use(block)

inline fun <T> Connection.prepareStatement(sql: String, block: (PreparedStatement) -> T) = prepareStatement(sql).use(block)
inline fun <T> PreparedStatement.executeQuery(block: (ResultSet) -> T) = executeQuery().use(block)

fun <T> DataSource.withTransaction(block: (Connection) -> T): T? {
    connection.use { connection ->
        try {
            connection.autoCommit = false

            val result = block(connection)

            connection.commit()

            return result
        } catch (e: Exception) {
            log.error("Det skjedde noe galt under eksekvering av SQL", e)

            connection.rollback()

            return null
        }
    }
}

fun <T> DataSource.executeQueryAndMapPreparedStatement(
    sql: String, setterBlock: (PreparedStatement) -> Unit, resultMapper: (ResultSet) -> T
): List<T>? {
    return this.withTransaction { connection ->
        connection.prepareStatement(sql) { preparedStatement ->
            setterBlock(preparedStatement)
            preparedStatement.executeQuery() { resultSet ->
                generateSequence {
                    if (resultSet.next()) resultMapper(resultSet) else null
                }.toList()
            }
        }
    }
}

fun Connection.prepareAndExecuteUpdate(sql: String, setterBlock: (PreparedStatement) -> Unit) {
    return this.prepareStatement(sql) { preparedStatement ->
        setterBlock(preparedStatement)
        preparedStatement.executeUpdate()
    }
}

fun Connection.prepareBatchAndExecuteUpdate(sql: String, setterBlocks: List<(PreparedStatement) -> Unit>) {
    return this.prepareStatement(sql) { preparedStatement ->
        setterBlocks.forEach { setterBlock ->
            setterBlock(preparedStatement)
            preparedStatement.addBatch()
        }
        preparedStatement.executeBatch()
    }
}

