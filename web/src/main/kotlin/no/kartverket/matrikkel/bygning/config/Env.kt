package no.kartverket.matrikkel.bygning.config

enum class Env {
    LOCAL,
    SKIP,
    ;

    companion object {
        // Checks if the app is running on k8s/SKIP
        fun current(): Env =
            when (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
                true -> SKIP
                false -> LOCAL
            }

        fun isLocal() = current() == LOCAL
    }
}
