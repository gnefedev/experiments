package com.gnefedev.coroutine

import com.github.mauricio.async.db.Connection
import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.pool.ObjectFactory
import com.github.mauricio.async.db.pool.PoolConfiguration
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.github.mauricio.async.db.util.NettyUtils
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.stereotype.Component
import scala.concurrent.ExecutionContext
import java.nio.charset.Charset
import java.util.concurrent.ForkJoinPool
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class AsyncPgsql (
        private val poolConfiguration: org.apache.tomcat.jdbc.pool.PoolConfiguration,
        private val dataSourceProperties: DataSourceProperties
) {
    lateinit var connectionPool: ConnectionPool<out Connection>

    @PostConstruct
    fun init() {
        val configuration = URLParser.parse(
                "${dataSourceProperties.url}?user=${dataSourceProperties.username}&password=${dataSourceProperties.password}",
                Charset.forName("UTF-8")
        )
        val factory: ObjectFactory<PostgreSQLConnection> = PostgreSQLConnectionFactory(configuration,
                NettyUtils.DefaultEventLoopGroup(),
                ExecutionContext.fromExecutor(ForkJoinPool.commonPool()))
        connectionPool = ConnectionPool(
                factory,
                PoolConfiguration(
                        poolConfiguration.maxActive,
                        poolConfiguration.maxIdle.toLong(),
                        poolConfiguration.maxWait / 30,
                        10_000
                ),
                ExecutionContext.fromExecutor(ForkJoinPool.commonPool())
        )
    }

    @PreDestroy
    fun tearDown() {
        runBlocking {
            connectionPool.close().await()
        }
    }
}

