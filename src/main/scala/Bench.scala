import com.bitlove.memcached.Memcached
import compat.Platform.currentTime

object Bench {
  def apply(desc: String, count: Int, f: => Any): Unit = {
    println(desc)
    (1 to count).foreach { _ =>
      val start = currentTime
      f
      println(currentTime - start)
    }
  }
}

object Runbench {
  def main(args: Array[String]) = {
    val c = new Memcached("localhost", 11211)

    Bench("shiit", 5, {
      (1 to 20000).foreach { i =>
        c.set("a".getBytes, "fuuuuuuuuu".getBytes)
        c.get("a".getBytes)
      }
    })

    val key1  = ("Short"        * 1 ).getBytes
    val key2  = ("Sym1-2-3::45" * 8 ).getBytes
    val key3  = ("Long"         * 40).getBytes
    val key4  = ("Medium"       * 8 ).getBytes

    val counterKey = "counter".getBytes

    val value = Array(4, 8, 91, 0).map(_.toByte)

    val iterations = 2500

    Bench("set", 5, {
      (1 to iterations).foreach { i =>
        c.set(key1, value)
        c.set(key2, value)
        c.set(key3, value)
        c.set(key1, value)
        c.set(key2, value)
        c.set(key3, value)
      }
    })

    Bench("get", 5, {
      (1 to iterations).foreach { i =>
        c.get(key1)
        c.get(key2)
        c.get(key3)
        c.get(key1)
        c.get(key2)
        c.get(key3)
      }
    })

    Bench("mixed", 5, {
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
    })

    Bench("incr/decr", 5, {
      (1 to iterations).foreach { i =>
        c.incr(counterKey, 1, default = Some(1))
      }

      (1 to iterations).foreach { i =>
        c.decr(counterKey, 1)
      }
    })
  }
}