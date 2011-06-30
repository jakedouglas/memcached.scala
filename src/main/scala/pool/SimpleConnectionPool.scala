package com.bitlove.memcached.pool

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit

class TimeoutError(message: String) extends Error(message)
class SimpleConnectionPool[Conn](connectionFactory: ConnectionFactory[Conn],
                                 max:     Int = 20,
                                 timeout: Int = 500000)
      extends ConnectionPool[Conn] {

  private val size = new AtomicInteger(0)
  private val pool = new ArrayBlockingQueue[Conn](max)

  def apply[A](f: Conn => A): A = {
    val connection = borrow

    try {
      val result = f(connection)
      giveBack(connection)
      result
    } catch {
      case t: Throwable =>
        invalidate(connection)
        throw t
    }
  }

  private def borrow: Conn = {
    Option(pool.poll) match {
      case Some(conn) => {
        connectionFactory.validate(conn) match {
          case true  => conn
          case false => invalidate(conn); borrow
        }
      }
      case None       => createOrBlock
    }
  }

  private def giveBack(connection: Conn): Unit = {
    pool.offer(connection)
  }

  private def invalidate(connection: Conn): Unit = {
    connectionFactory.destroy(connection)
    size.decrementAndGet
  }

  private def createOrBlock: Conn = {
    size.get match {
      case e: Int if e == max => block
      case _                  => create
    }
  }

  private def create: Conn = {
    size.incrementAndGet match {
      case e: Int if e > max => size.decrementAndGet; borrow
      case e: Int            => connectionFactory.create
    }
  }

  private def block: Conn = {
    Option(pool.poll(timeout, TimeUnit.NANOSECONDS)) match {
      case Some(conn) => conn
      case None       => throw new TimeoutError("Couldn't acquire a connection in %d nanoseconds.".format(timeout))
    }
  }
}
