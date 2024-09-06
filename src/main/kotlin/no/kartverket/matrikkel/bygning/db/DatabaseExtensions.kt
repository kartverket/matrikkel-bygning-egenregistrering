package no.kartverket.matrikkel.bygning.db

import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

inline fun <T> DataSource.connection(block: (Connection) -> T) = connection.use(block)

inline fun <T> Connection.createStatement(block: (Statement) -> T) = createStatement().use(block)
inline fun <T> Statement.executeQuery(sql: String, block: (ResultSet) -> T) = executeQuery(sql).use(block)

inline fun <T> Connection.prepareStatement(sql: String, block: (PreparedStatement) -> T) = prepareStatement(sql).use(block)
inline fun <T> PreparedStatement.executeQuery(block: (ResultSet) -> T) = executeQuery().use(block)

fun <T> DataSource.executeQueryAndMapPreparedStatement(
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

fun Connection.prepareAndExecuteUpdate(sql: String, vararg setters: (PreparedStatement) -> Unit): Int {
    return this.prepareStatement(sql) { preparedStatement ->
        setters.forEach { it(preparedStatement) }
        preparedStatement.executeUpdate()
    }
}

fun Connection.prepareBatchAndExecuteUpdate(sql: String, batchSetters: List<(PreparedStatement) -> Unit>): List<Int> {
    return this.prepareStatement(sql) { preparedStatement ->
        batchSetters.forEach { setter ->
            setter(preparedStatement)
            preparedStatement.addBatch()
        }
        preparedStatement.executeBatch().toList()
    }
}

fun <T> DataSource.withTransaction(block: (Connection) -> Result<T>): Result<T> {
    connection.use { connection ->
        try {
            connection.autoCommit = false

            val result = block(connection)

            when (result) {
                is Result.Success -> connection.commit()
                is Result.ErrorResult -> connection.rollback()
            }

            return result

        } catch (_: SQLException) {
            connection.rollback()

            return Result.ErrorResult(
                error = ErrorDetail(
                    detail = "Noe gikk galt",
                ),
            )
        }
    }
}

