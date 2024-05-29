package no.kartverket.matrikkel.bygning.repositories

import no.kartverket.matrikkel.bygning.models.BygningDTO
import no.kartverket.matrikkel.bygning.models.EnergikildeDTO
import no.kartverket.matrikkel.bygning.models.OppvarmingDTO
import java.sql.Connection
import java.sql.ResultSet

class BygningRepository(private val dbConnection: Connection) {
    fun getBygningerIds(): List<String> {
        val statement = dbConnection.createStatement()

        val resultSet = statement.executeQuery(
            """
            select id from bygning b
        """.trimIndent()
        )

        return resultSet.toBygningIds()
    }

    fun getBygninger(): List<BygningDTO> {
        val statement = dbConnection.createStatement()

        val resultSet = statement.executeQuery(
            """
            select * from bygning b
        """.trimIndent()
        )

        return resultSet.toBygningDTOs()
    }

    fun getEnergikilderForBygning(bygningId: String): List<EnergikildeDTO> {
        val statement = dbConnection.prepareStatement(
            """
            select id, navn from energikilde e
            left join "bygning_energikilder" be on e."id" = be."energikilde_id"
            where be.bygning_id = ?;
        """.trimIndent()
        )

        statement.setString(1, bygningId)

        val resultSet = statement.executeQuery()

        return resultSet.toEnergikildeDTOs()
    }

    fun getOppvarmingerForBygning(bygningId: String): List<OppvarmingDTO> {
        val statement = dbConnection.prepareStatement(
            """
            select id, navn from oppvarming o
            left join "bygning_oppvarminger" bo on o."id" = bo."oppvarming_id"
            where bo.bygning_id = ?;
        """.trimIndent()
        )

        statement.setString(1, bygningId)

        val resultSet = statement.executeQuery()

        return resultSet.toOppvarmingDTOs()
    }
}

fun ResultSet.toEnergikildeDTOs(): List<EnergikildeDTO> {
    val energikilder: MutableList<EnergikildeDTO> = emptyList<EnergikildeDTO>().toMutableList()

    while (this.next()) {
        energikilder.add(
            EnergikildeDTO(
                id = this.getInt("id"), navn = this.getString("navn")
            )
        )
    }

    return energikilder
}

fun ResultSet.toOppvarmingDTOs(): List<OppvarmingDTO> {
    val oppvarminger: MutableList<OppvarmingDTO> = emptyList<OppvarmingDTO>().toMutableList()

    while (this.next()) {
        oppvarminger.add(
            OppvarmingDTO(
                id = this.getInt("id"), navn = this.getString("navn")
            )
        )
    }

    return oppvarminger
}

fun ResultSet.toBygningIds(): List<String> {
    val ids: MutableList<String> = mutableListOf()

    while (this.next()) {
        ids.add(this.getString("id"))
    }

    return ids
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