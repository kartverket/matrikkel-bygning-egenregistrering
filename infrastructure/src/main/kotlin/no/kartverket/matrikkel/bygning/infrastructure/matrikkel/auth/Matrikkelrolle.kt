package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth

enum class Matrikkelrolle(
    /** Må eksplisitt liste alle roller, også de som arver fra andre roller i matrikkelen. */
    val akseptableRolleIds: Set<Long>
) {
    BerettigetInteresse(
        hashSetOf(
            0L, // Kommunalt innsyn uten fnr
            1L, // Kommunalt innsyn med fnr
            11L, // Innsyn
            12L, // Innsyn uten fnr
            13L, // Innsyn med fnr
            6L, // Endringslogg
            3L, // Matrikkelfører
            32L, // Berettiget interesse
        ),
    ),
    InnsynUtenPersondata(
        hashSetOf(
            0L, // Kommunalt innsyn uten fnr
            1L, // Kommunalt innsyn med fnr
            11L, // Innsyn
            12L, // Innsyn uten fnr
            13L, // Innsyn med fnr
            6L, // Endringslogg
            3L, // Matrikkelfører
        ),
    ),
    InnsynMedPersondata(
        hashSetOf(
            1L, // Kommunalt innsyn med fnr
            13L, // Innsyn med fnr
            3L, // Matrikkelfører
        ),
    )
}
