package com.bitlove.memcached

import com.bitlove.memcached.protocol._
import java.nio.ByteBuffer

object RequestBuilder {
  def incrOrDecr(opcode:  Byte,
                 key:     Array[Byte],
                 count:   Long,
                 ttl:     Int,
                 default: Option[BigInt]): Array[ByteBuffer] = {

    val expiration = default match {
      case None         => 0xFFFFFFFF
      case Some(number) => ttl
    }

    val request = newRequest(44, opcode).putShort(2,  key.size.toShort)
                                        .put     (4,  20.toByte)
                                        .putInt  (8,  20 + key.size)
                                        .putLong (24, count)
                                        .putInt  (40, expiration)

    default match {
      case None         => ()
      case Some(number) => {
        (0 to 7).foreach { i =>
          request.put(i + 32, (number >> (7 - i) * 8).toByte)
        }
      }
    }

    Array(request, ByteBuffer.wrap(key))
  }

  def flush(after: Option[Int]): Array[ByteBuffer] = {
    after match {
      case None          => Array(newRequest(24, Ops.Flush))
      case Some(seconds) => Array(newRequest(28, Ops.Flush).put   (4, 4.toByte)
                                                           .putInt(8, 4)
                                                           .putInt(24, seconds))
    }
  }

  def get(key: Array[Byte]): Array[ByteBuffer] = {
    Array(newRequest(24, Ops.Get).putShort(2, key.size.toShort)
                                 .putInt  (8, key.size),
          ByteBuffer.wrap(key))
  }

  def storageRequest(opcode: Byte,
                     key:    Array[Byte],
                     value:  Array[Byte],
                     flags:  Int,
                     ttl:    Int): Array[ByteBuffer] = {

    Array(newRequest(32, opcode).putShort (2,  key.size.toShort)
                                .put      (4,  8.toByte)
                                .putInt   (8,  key.size + value.size + 8)
                                .putInt   (24, flags)
                                .putInt   (28, ttl),
          ByteBuffer.wrap(key),
          ByteBuffer.wrap(value))
  }

  def appendOrPrepend(opcode: Byte,
                      key:    Array[Byte],
                      value:  Array[Byte]): Array[ByteBuffer] = {

    Array(newRequest(24, opcode).putShort (2,  key.size.toShort)
                                .putInt   (8,  key.size + value.size),
          ByteBuffer.wrap(key),
          ByteBuffer.wrap(value))
  }

  def delete(key: Array[Byte]): Array[ByteBuffer] = {
    Array(newRequest(24, Ops.Delete).putShort (2,  key.size.toShort)
                                    .putInt   (8,  key.size),
          ByteBuffer.wrap(key))
  }

  private def newRequest(size: Int, opcode: Byte): ByteBuffer = {
    ByteBuffer.allocate(size).put(0, Packets.Request)
                             .put(1, opcode)
  }
}