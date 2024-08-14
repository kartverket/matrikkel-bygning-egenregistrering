create table bygning (

);

create table bruksenhet (

);

create table egenregistrering (
    id,
    metadata her?
);

create table registrering (
    id,
    egenregistrering_id,
    registreringsTidspunkt,
    gyldigFra?,
    gyldigTil?
);

create table byggeaarregistrering (
    id = registering_id (slipper at alle de forskjellige registreringstabellene må inneholde de samme feltene?),
    byggeaar,
);

--
-- @Serializable
-- data class RegistreringMetadataRequest(
--     val registreringstidspunkt: Instant,
--     val gyldigFra: LocalDate?,
--     val gyldigTil: LocalDate?,
-- )
--
-- @Serializable
-- data class ByggeaarRegistrering(
--     val byggeaar: Int, val metadata: RegistreringMetadataRequest
-- )
--
-- @Serializable
-- data class BruksarealRegistrering(
--     val bruksareal: Double, val metadata: RegistreringMetadataRequest
-- )
--
-- @Serializable
-- data class VannforsyningsRegistrering(
--     val vannforsyning: VannforsyningsKode, val metadata: RegistreringMetadataRequest
-- )
--
-- @Serializable
-- data class AvlopRegistrering(
--     val avlop: AvlopsKode, val metadata: RegistreringMetadataRequest
-- )
--
-- @Serializable
-- data class EnergikildeRegistrering(
--     val energikilder: List<EnergikildeKode>, val metadata: RegistreringMetadataRequest
-- )
--
-- @Serializable
-- data class OppvarmingRegistrering(
--     val oppvarminger: List<OppvarmingsKode>, val metadata: RegistreringMetadataRequest
-- )
--
-- @Serializable
-- data class BygningsRegistrering(
--     val bruksareal: BruksarealRegistrering?,
--     val byggeaar: ByggeaarRegistrering?,
--     val vannforsyning: VannforsyningsRegistrering?,
--     val avlop: AvlopRegistrering?
-- )
--
-- @Serializable
-- data class BruksenhetRegistrering(
--     val bruksenhetId: Long,
--     val bruksareal: BruksarealRegistrering?,
--     val energikilde: EnergikildeRegistrering?,
--     val oppvarming: OppvarmingRegistrering?
-- )
--
-- @Serializable
-- data class EgenregistreringRequest(
--     val bygningsRegistrering: BygningsRegistrering, val bruksenhetRegistreringer: List<BruksenhetRegistrering>
-- )
