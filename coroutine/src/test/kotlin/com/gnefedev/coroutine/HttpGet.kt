package com.gnefedev.coroutine

import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.Channels
import java.nio.channels.SocketChannel
import java.nio.channels.WritableByteChannel
import java.nio.charset.Charset


private val yandexUrl = URL("http://ya.ru/")

class HttpGet {
    @Ignore
    @Test
    fun sync() {
        val response = getSync()
        assertTrue(response, response.startsWith("""<!DOCTYPE html><html class="i-ua_js_no i-ua_css_standart i-ua_browser_ i-ua_browser_desktop i-ua_platform_other" lang="ru">"""))
        assertTrue(response, response.endsWith("""</body></html>"""))
    }

    @Test
    fun async() {
        val response = getAsync()
        assertTrue(response, response.startsWith("""HTTP/1.1 302 Found"""))
        assertTrue(response, response.endsWith("Connection: Close\r\n\r\n"))
    }

    @Test
    fun trueAsync() {
        val response = getTrueAsync()
        assertTrue(response, response.startsWith("""HTTP/1.1 302 Found"""))
        assertTrue(response, response.endsWith("Connection: Close\r\n\r\n"))
    }

    private fun getSync(): String {
        val url = yandexUrl
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"

        val input = BufferedReader(InputStreamReader(connection.inputStream))
        var inputLine: String?
        val response = StringBuilder()
        do {
            inputLine = input.readLine()
            if (inputLine != null) {
                response.append(inputLine)
            }
        } while (inputLine != null)
        input.close()

        return response.toString()
    }

    private fun getTrueAsync(): String {
        val uri = yandexUrl.toURI()
        val socketChannel = AsynchronousSocketChannel.open()
        val connect = socketChannel.connect(InetSocketAddress(uri.host, 80))
        connect.get()

        socketChannel.read(ByteBuffer.allocate(10))
        return ""
    }

    private fun getAsync(): String {
        val uri = yandexUrl.toURI()

        SocketChannel.open(InetSocketAddress(uri.host, 80)).use {
            val request = "GET ${uri.rawPath}${if (uri.rawQuery == null) "" else '?' + uri.rawQuery} HTTP/1.1\r\n" +
                    "Host: ${uri.host}\r\n" +
                    "Connection: close\r\n" +
                    "User-Agent: ${HttpGet::class.java.name}\r\n" +
                    "\r\n"

            val requestChars = CharBuffer.wrap(request)

            val charset = Charset.forName("UTF-8")

            val requestBytes = charset.encode(requestChars)

            it.write(requestBytes)

            val out = ByteArrayOutputStream()
            val destination: WritableByteChannel // Channel to write to it
            destination = Channels.newChannel(out)

            val data = ByteBuffer.allocateDirect(32 * 1024)

            while (it.read(data) != -1) {
                data.flip()

                while (data.hasRemaining())
                    destination.write(data)

                data.clear()
            }
            return out.toString("UTF-8")
        }
    }
}