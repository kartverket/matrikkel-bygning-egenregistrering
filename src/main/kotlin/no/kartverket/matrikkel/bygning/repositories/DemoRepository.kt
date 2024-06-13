package no.kartverket.matrikkel.bygning.repositories

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.EnergikildeDTO
import java.sql.Connection
import java.sql.ResultSet

@Serializable
data class DemospokelseDTO(
    val id: Int,
    val spokelsesnavn: String,
)

class DemoRepository(private val dbConnection: Connection) {
    fun getDemospokelser(): List<DemospokelseDTO> {
        val statement = dbConnection.createStatement()

        val resultSet = statement.executeQuery(
            """
            select * from demospokelser;
        """.trimIndent()
        )

        return resultSet.toDemospokelseDTO()
    }

    fun ResultSet.toDemospokelseDTO(): List<DemospokelseDTO> {
        val demospokelser: MutableList<DemospokelseDTO> = emptyList<DemospokelseDTO>().toMutableList()

        while (this.next()) {
            demospokelser.add(
                DemospokelseDTO(
                    id = this.getInt("id"), spokelsesnavn = this.getString("spokelsesnavn")
                )
            )
        }

        return demospokelser
    }
}
