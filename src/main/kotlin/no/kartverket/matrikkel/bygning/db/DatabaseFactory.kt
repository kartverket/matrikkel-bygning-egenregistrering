import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class DatabaseFactory(private val config: DatabaseConfig) {
    private var dataSource: DataSource

    init {
        dataSource = createHikariDataSource()
    }

    private fun createHikariDataSource(): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            driverClassName = config.driverClassName
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = config.maxPoolSize

            config.defaultSchema?.let { schema ->
                addDataSourceProperty("currentSchema", schema)
                addDataSourceProperty("searchpath", schema)
            }
        }
        return HikariDataSource(hikariConfig)
    }

    fun runFlywayMigrations() {
        val flyway = Flyway.configure()
            .validateMigrationNaming(true)
            .createSchemas(true)
            .defaultSchema("bygning")
            .outputQueryResults(true)
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
        flyway.migrate()
    }

    fun getDataSource(): DataSource = dataSource
}

data class DatabaseConfig(
    val driverClassName: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int,
    val defaultSchema: String? = null
)