package no.kartverket.matrikkel.bygning.repositories

import java.sql.Connection
import java.sql.ResultSet

class BygningRepository(private val dbConnection: Connection) {
    fun getBygningIds(): List<String> {
        val statement = dbConnection.createStatement()

        val resultSet = statement.executeQuery(
            """
            select id from bygning b
        """.trimIndent()
        )

        return resultSet.toBygningIds();
    }
}

fun ResultSet.toBygningIds(): List<String> {
    val ids: MutableList<String> = emptyList<String>().toMutableList()


    while (this.next()) {
        ids.add(this.getString("id"))
    }

    return ids;
}