package com.bitlove.memcached.pool

trait ConnectionPool[Connection] {
  def apply[A](f: Connection => A): A
}
