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
  }
}