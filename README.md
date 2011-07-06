memcached.scala
===============

A simple and hopefully fast memcached client for scala. It uses java nio
and implements the binary protocol. This client has not been exercised
in production yet, so there are probably bugs. I did my best to expose the
correct semantics per my interpretation of the protocol and looking at
other clients, but it may not be perfect, so feel free to disagree with
me and we can fix it.

Setup
-----

Just add it to your sbt project definition:

```scala
val memcached = "com.bitlove" %%
                  "memcached" %
                    "0.0.1" from
                      "http://cloud.github.com/downloads/jakedouglas/memcached.scala/memcached.scala_2.8.1-0.0.1.jar"
```

Simple usage
------------

```scala
import com.bitlove.memcached.Memcached

val c     = new Memcached("localhost", 11211)
val key   = "some key".getBytes
val value = "my value".getBytes

c.set(key, value)

c.get match {
  case None        => println("nothing there!")
  case Some(bytes) => println(new String(bytes))
}
```

Multiple threads
----------------

The client itself is not intended to be used by multiple threads at once,
so you need to use a connection pool for a multi-threaded program. You
can use your own favorite connection pool, or a simple one is included:

```scala
import com.bitlove.memcached.pool.MemcachedPool

val pool = new MemcachedPool("localhost", 11211)

pool { c =>
  c.get(...)
}
```

Transcoding
-----------

Transcoders allow you to to pass and receive objects of your own type,
and accomodate custom serialization, compression, etc. These are
intended to be chainable. The 'outermost' transcoder needs to be able to
produce and decode values of Array[Byte]. Transcoders have access to the
"flags" field that get stored in memcached, used for designating whether
a value is serialized or compressed, etc.

See [these files](https://github.com/jakedouglas/memcached.scala/tree/master/src/main/scala/transcoding) to understand how transcoders are assembled and chained.

Transcoders can be put to use at different levels of granularity:

* Provide a transcoder to the Memcached constructor to be used by default for all requests.
* Define an implicit transcoder to use in a certain scope.
* Pass in a transcoder on a per-request basis.

Docs
----

For now, please refer to the [scaladocs](http://jakedouglas.github.com/memcached.scala).
Eventually there will be more here.

Issues
------

I'll try to fix problems when I have time. Pull requests are welcome and
encouraged. If you submit a pull request, please include tests.

Thanks
------
@al3x, @jamesgolick and @moonpolysoft all helped answer some questions
at one point or another.
