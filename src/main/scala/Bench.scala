import com.bitlove.memcached.Memcached
import com.bitlove.memcached.transcoding.{ByteArrayTranscoder, GzipByteArrayTranscoder}
import compat.Platform.currentTime

object Bench {
  def apply(desc: String, count: Int)(f: => Any): Unit = {
    println("==============================")
    println(desc + " - running %d iterations".format(count))

    val times = (1 to count).map { _ =>
      val start = currentTime
      f
      val duration = currentTime - start
      duration
    }

    println("Min: %d Max: %d Avg: %d".format(times.min, times.max, (times.sum / times.size)))
    println("(%s)".format(times.mkString(", ")))
  }
}

// mostly copied from mperham's dalli
object Runbench {
  def main(args: Array[String]) = {
    val c = new Memcached("localhost", 11211)

    val key1  = ("Short"        * 1 ).getBytes
    val key2  = ("Sym1-2-3::45" * 8 ).getBytes
    val key3  = ("Long"         * 40).getBytes
    val key4  = ("Medium"       * 8 ).getBytes

    val counterKey = "counter".getBytes

    val value = Array(4, 8, 91, 0).map(_.toByte)

    val iterations = 2500

    Bench("set", 5) {
      (1 to iterations).foreach { i =>
        c.set(key1, value)
        c.set(key2, value)
        c.set(key3, value)
        c.set(key1, value)
        c.set(key2, value)
        c.set(key3, value)
      }
    }

    Bench("get", 5) {
      (1 to iterations).foreach { i =>
        c.get(key1)
        c.get(key2)
        c.get(key3)
        c.get(key1)
        c.get(key2)
        c.get(key3)
      }
    }

    Bench("mixed", 5) {
      (1 to iterations).foreach { i =>
        c.set(key1, value)
        c.set(key2, value)
        c.set(key3, value)
        c.get(key1)
        c.get(key2)
        c.get(key3)
        c.set(key1, value)
        c.get(key1)
        c.set(key2, value)
        c.get(key2)
        c.set(key3, value)
        c.get(key3)
      }
    }

    Bench("incr/decr", 5) {
      (1 to iterations).foreach { i =>
        c.incr(counterKey, 1, default = Some(1))
      }

      (1 to iterations).foreach { i =>
        c.decr(counterKey, 1)
      }
    }

    Bench("huge values - uncompressed", 5) {
      val hugeValue = scala.util.Random.nextString(50000).getBytes
      c.set(key1, hugeValue)

      (1 to iterations).foreach { i =>
        c.get(key1)
      }
    }

    Bench("huge values - with gzip compression", 5) {
      val hugeValue = scala.util.Random.nextString(50000).getBytes
      implicit val transcoder = new GzipByteArrayTranscoder
      c.set(key1, hugeValue)

      (1 to iterations).foreach { i =>
        c.get(key1)
      }
    }
  }
}
