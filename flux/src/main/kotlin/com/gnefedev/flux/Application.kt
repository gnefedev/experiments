package com.gnefedev.flux

import com.gnefedev.flux.cats.AsyncCatsService
import com.gnefedev.flux.cats.CatsAsyncController
import com.gnefedev.flux.cats.CatsSyncController
import com.gnefedev.flux.cats.SyncCatsService
import com.spotify.folsom.MemcacheClient
import com.spotify.folsom.MemcacheClientBuilder
import mu.KLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.postgresql.ds.PGPoolingDataSource
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import reactor.ipc.netty.http.server.HttpServer
import java.io.Serializable
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class DatasourceConfig {
    @Bean
    fun datasource(): DataSource = PGPoolingDataSource().apply {
        password = "123456"
        user = "for_benchmark"
        databaseName = "for_benchmark"
        portNumber = 5433
        maxConnections = DatasourceConfig.maxConnections
    }

    @Bean
    fun platformTransactionManager(dataSource: DataSource) = DataSourceTransactionManager(dataSource)

    @Bean
    fun jdbcTemplate(dataSource: DataSource) = JdbcTemplate(dataSource)

    @Bean
    fun transactionTemplate(platformTransactionManager: PlatformTransactionManager) =
        TransactionTemplate(platformTransactionManager)

    companion object {
        const val maxConnections = 8
    }
}

@Configuration
class MemcachedConfig {
    @Bean
    fun memcachedClient(): MemcacheClient<Serializable> = MemcacheClientBuilder.newSerializableObjectClient()
        .withAddress("localhost", 11211)
        .connectBinary()
}

@Configuration
@EnableWebMvc
@Import(DatasourceConfig::class, MemcachedConfig::class)
class SyncApplication {
    @Bean
    fun catsService(jdbcTemplate: JdbcTemplate) = SyncCatsService(jdbcTemplate)

    @Bean
    fun catsController(catsService: SyncCatsService, memcacheClient: MemcacheClient<Serializable>) = CatsSyncController(catsService, memcacheClient)
}

@Configuration
@EnableWebFlux
@Import(DatasourceConfig::class, MemcachedConfig::class)
class AsyncApplication {
    @Bean
    fun catsService(jdbcTemplate: JdbcTemplate, transactionTemplate: TransactionTemplate) =
        AsyncCatsService(jdbcTemplate, transactionTemplate)

    @Bean
    fun catsController(catsService: AsyncCatsService, memcacheClient: MemcacheClient<Serializable>) = CatsAsyncController(catsService, memcacheClient)
}

private val logger = KLogging()

fun main(args: Array<String>) {
    val port = 8080

    val serverType = System.getenv("SERVER_TYPE")

    logger.logger.info { "serverType: $serverType" }

    if (serverType.toLowerCase() == "jetty") {
        val maxThreads = 100
        val server = Server(QueuedThreadPool(maxThreads))
        val connector = ServerConnector(server)
        connector.port = port
        server.connectors = arrayOf(connector)
        server.handler = ServletContextHandler().apply {
            contextPath = "/"
            val context = AnnotationConfigWebApplicationContext()
            context.register(SyncApplication::class.java)
            addServlet(ServletHolder(DispatcherServlet(context)), "/")
            addEventListener(ContextLoaderListener(context))
        }
        server.start()
        server.join()
    } else {
        val handler = WebHttpHandlerBuilder
            .applicationContext(AnnotationConfigApplicationContext(AsyncApplication::class.java))
            .build()
        HttpServer.create(port)
            .newHandler(ReactorHttpHandlerAdapter(handler))
            .block()!!
            .channel()
            .closeFuture()
            .sync()
    }
}

