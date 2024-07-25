package no.kartverket.matrikkel.bygning.repositories

import no.kartverket.matrikkel.bygning.models.BygningDTO
import java.sql.ResultSet
import javax.sql.DataSource

class BygningRepository(private val dataSource: DataSource) {
    fun getBygninger(): List<BygningDTO> {
        val sql = "SELECT * FROM bygning;"
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery(sql)

                if (resultSet.next()) {
                    return resultSet.toBygningDTOs()
                } else {
                    return emptyList()
                }
            }
        }
    }
}

fun ResultSet.toBygningDTOs(): List<BygningDTO> {
    val bygninger: MutableList<BygningDTO> = mutableListOf()

    while (this.next()) {
        bygninger.add(
            BygningDTO(
                id = this.getString("id"),
                byggaar = this.getInt("byggeaar"),
                vann = this.getBoolean("vann"),
                avlop = this.getBoolean("avlop"),
            )
        )
    }

    return bygninger
}