package com.gnefedev.coroutine

import com.github.mauricio.async.db.Connection
import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.pool.ObjectFactory
import com.github.mauricio.async.db.pool.PoolConfiguration
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.github.mauricio.async.db.util.NettyUtils
import org.springframework.stereotype.Component
import scala.concurrent.ExecutionContext
import java.nio.charset.Charset
import java.util.concurrent.ForkJoinPool
import javax.annotation.PostConstruct

@Component
class AsyncPgsql {
    lateinit var connectionPool: ConnectionPool<out Connection>
    @PostConstruct
    fun init() {
        val configuration = URLParser.parse(
                "jdbc:postgresql://localhost:5432/for_benchmark?user=for_benchmark&password=123456",
                Charset.forName("UTF-8")
        )
        val factory: ObjectFactory<PostgreSQLConnection> = PostgreSQLConnectionFactory(configuration,
                NettyUtils.DefaultEventLoopGroup(),
                ExecutionContext.fromExecutor(ForkJoinPool.commonPool()))
        connectionPool = ConnectionPool(factory, PoolConfiguration(100, 10, 100, 1000), ExecutionContext.fromExecutor(ForkJoinPool.commonPool()))
    }
}

