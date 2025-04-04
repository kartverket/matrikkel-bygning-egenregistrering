package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.AvsluttEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.AvsluttFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.AvsluttFeltRegistrering.AvsluttAvlop
import no.kartverket.matrikkel.bygning.application.models.AvsluttFeltRegistrering.AvsluttVannforsyning
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.EgenregistreringBase
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.AvlopFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.BruksarealFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.ByggeaarFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.EnergikildeFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.OppvarmingFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.VannforsyningFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.KorrigerEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.RegistrerEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.Konflikt
import no.kartverket.matrikkel.bygning.application.models.error.ValidationError

class EgenregistreringInputValidator {
    companion object {
        fun valider(
            bruksenhet: Bruksenhet,
            egenregistrering: EgenregistreringBase,
        ): Result<Unit, DomainError> = bruksenhet.valider(egenregistrering)
    }
}

private fun Bruksenhet.valider(egenregistrering: EgenregistreringBase): Result<Unit, DomainError> =
    when (egenregistrering) {
        is RegistrerEgenregistrering ->
            when (harRegistrertDataForFelt(egenregistrering.feltRegistrering)) {
                true ->
                    Err(
                        Konflikt(
                            "Feltet er allerede registrert for bruksenheten. Du kan kun korrigere nåværende verdi eller avslutte feltet.",
                        ),
                    )
                false -> Ok(Unit)
            }
        is KorrigerEgenregistrering ->
            when (harRegistrertDataForFelt(egenregistrering.feltRegistrering)) {
                true -> Ok(Unit)
                else ->
                    Err(
                        Konflikt(
                            "Feltet er ikke registrert. Legg til en registrerering for feltet for å redigere dataen.",
                        ),
                    )
            }
        is AvsluttEgenregistrering ->
            when (kanSetteOpphoersaar(egenregistrering.feltRegistrering)) {
                true -> Ok(Unit)
                false ->
                    Err(
                        ValidationError(
                            "Ikke mulig å sette opphørsår eller opphørsåret er ikke gyldig.",
                        ),
                    )
            }
    }

private fun Bruksenhet.harRegistrertDataForFelt(feltRegistrering: FeltRegistrering): Boolean =
    when (feltRegistrering) {
        is ByggeaarFeltRegistrering -> this.byggeaar.egenregistrert != null
        is VannforsyningFeltRegistrering -> this.vannforsyning.egenregistrert != null
        is AvlopFeltRegistrering -> this.avlop.egenregistrert != null
        is BruksarealFeltRegistrering.TotaltBruksarealFeltRegistrering -> this.totaltBruksareal.egenregistrert != null
        is BruksarealFeltRegistrering.TotaltOgEtasjeBruksarealFeltRegistrering ->
            this.totaltBruksareal.egenregistrert != null
        is EnergikildeFeltRegistrering.Energikilder ->
            this.energikilder.egenregistrert
                ?.map { it.data }
                ?.any { energikildeKode -> energikildeKode in feltRegistrering.energikilder.map { it.energikilde } } ?: false
        is EnergikildeFeltRegistrering.HarIkke -> TODO()
        is OppvarmingFeltRegistrering.Oppvarming ->
            this.oppvarming.egenregistrert
                ?.map { it.data }
                ?.any { oppvarmingKode -> oppvarmingKode in feltRegistrering.oppvarming.map { it.oppvarming } } ?: false
        is OppvarmingFeltRegistrering.HarIkke -> TODO()
    }

private fun Bruksenhet.kanSetteOpphoersaar(egenregistrering: AvsluttFeltRegistrering): Boolean =
    when (egenregistrering) {
        is AvsluttVannforsyning ->
            this.vannforsyning.egenregistrert?.data == egenregistrering.vannforsyning &&
                this.vannforsyning.egenregistrert
                    .metadata
                    .gyldighetsperiode
                    .erGyldigOpphoersaar(egenregistrering.opphoersaar)

        is AvsluttAvlop ->
            this.avlop.egenregistrert?.data == egenregistrering.avlop &&
                this.avlop.egenregistrert
                    .metadata
                    .gyldighetsperiode
                    .erGyldigOpphoersaar(egenregistrering.opphoersaar)
    }
