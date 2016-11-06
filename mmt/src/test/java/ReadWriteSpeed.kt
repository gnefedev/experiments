import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import sun.misc.Unsafe
import java.io.File
import java.io.RandomAccessFile
import java.util.*

fun <T> List<T>.shuffle(): List<T> {
    Collections.shuffle(this)
    return this
}

fun benchmark(name: String, toMeasure: () -> Unit) {
    val before = Date()
    toMeasure.invoke()
    val after = Date()
    val time = after.time - before.time
    println("$name takes $time milliseconds")
}

class ReadWriteSpeed {
    val size = 500000
    val INT_SIZE_IN_BYTES = 4

    lateinit var samples: Array<Int>
    lateinit var indexes: Array<Int>

    @Before
    fun before() {
        val random = Random(System.currentTimeMillis())
        samples = Array(size, { random.nextInt() })
        indexes = Array(size, { it }).toList().shuffle().toTypedArray()
    }

    @After
    fun after() {
        println("")
    }

    @Test
    fun inMemory() {
        benchmark("inMemory all") {
            val array = Array(size) { samples[it]}

            benchmark("inMemory read") {
                indexes.forEach {
                    if (array[it].hashCode() == 0) {
                        println("")
                    }
                }
            }
        }
    }

    @Test
    fun offHeap() {
        benchmark("offHeap all") {
            val unsafeConstructor = Unsafe::class.java.getDeclaredConstructor()
            unsafeConstructor.isAccessible = true
            val unsafe = unsafeConstructor.newInstance()
            val address = unsafe.allocateMemory(size.toLong() * INT_SIZE_IN_BYTES)
            unsafe.setMemory(address, size.toLong() * INT_SIZE_IN_BYTES, 0)
            indexes.forEach { unsafe.putInt(index(address, it), samples[it]) }

            benchmark("offHeap read") {
                indexes.forEach {
                    if (unsafe.getInt(index(address, it)).hashCode() == 0) {
                        println("")
                    }
                }
            }

            unsafe.freeMemory(address)
        }
    }

    private fun index(address: Long, offset: Int): Long {
        return address + offset * INT_SIZE_IN_BYTES
    }

    @Test
    fun ssd() {
        benchmark("ssd all") {
            val tmpDir = File("/tmp/forTest")
            FileUtils.deleteDirectory(tmpDir)
            FileUtils.forceMkdir(tmpDir)

            val tmpFile = "$tmpDir/test.txt"
            File(tmpFile).createNewFile()
            var file = RandomAccessFile(tmpFile, "rw")
            for (i in 0..size - 1) {
                file.writeInt(samples[i])
            }

            file = RandomAccessFile(tmpFile, "r")
            benchmark("ssd read") {
                indexes.forEach {
                    file.seek(it.toLong() * INT_SIZE_IN_BYTES)
                    if (file.readInt().hashCode() == 0) {
                        println("")
                    }
                }
            }
        }
    }

    @Test
    fun hdd() {
        benchmark("hdd all") {
            val tmpDir = File("/media/gerakln/DATA/temp/forTest")
            FileUtils.deleteDirectory(tmpDir)
            FileUtils.forceMkdir(tmpDir)

            val tmpFile = "$tmpDir/test.txt"
            File(tmpFile).createNewFile()
            var file = RandomAccessFile(tmpFile, "rw")
            for (i in 0..size - 1) {
                file.writeInt(samples[i])
            }

            file = RandomAccessFile(tmpFile, "r")
            benchmark("hdd read") {
                indexes.forEach {
                    file.seek(it.toLong() * INT_SIZE_IN_BYTES)
                    if (file.readInt().hashCode() == 0) {
                        println("")
                    }
                }
            }
        }
    }
}

// output for 1.000.000
//
//offHeap read takes 122 milliseconds
//offHeap all takes 190 milliseconds
//
//inMemory read takes 35 milliseconds
//inMemory all takes 804 milliseconds
//
//hdd read takes 2447 milliseconds
//hdd all takes 141164 milliseconds
//
//ssd read takes 1961 milliseconds
//ssd all takes 8038 milliseconds

// output for 500.000
//
//offHeap read takes 34 milliseconds
//offHeap all takes 82 milliseconds
//
//inMemory read takes 22 milliseconds
//inMemory all takes 38 milliseconds
//
//hdd read takes 954 milliseconds
//hdd all takes 69547 milliseconds
//
//ssd read takes 941 milliseconds
//ssd all takes 4098 milliseconds

// output for 100.000
//
//offHeap read takes 13 milliseconds
//offHeap all takes 37 milliseconds
//
//inMemory read takes 7 milliseconds
//inMemory all takes 18 milliseconds
//
//hdd read takes 228 milliseconds
//hdd all takes 13952 milliseconds
//
//ssd read takes 195 milliseconds
//ssd all takes 811 milliseconds