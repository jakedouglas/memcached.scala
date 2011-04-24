package com.bitlove.memcached

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.net.InetSocketAddress
import com.bitlove.memcached.protocol._

class Memcached[T](host:       String,
                   port:       Int,
                   transcoder: Transcoder[T] = new ByteArrayTranscoder) {
  class ProtocolError(message: String) extends Error(message)

  private val addr    = new InetSocketAddress(host, port)
  private val channel = SocketChannel.open(addr)
  private val header  = ByteBuffer.allocate(24)

  def append(key:   Array[Byte],
             value: Array[Byte]): Boolean = {
    channel.write(RequestBuilder.appendOrPrepend(Ops.Append, key, value))
    handleResponse(Ops.Append, handleStorageResponse)
  }

  def prepend(key:   Array[Byte],
              value: Array[Byte]): Boolean = {
    channel.write(RequestBuilder.appendOrPrepend(Ops.Prepend, key, value))
    handleResponse(Ops.Prepend, handleStorageResponse)
  }

  def incr(key:     Array[Byte],
           count:   Long = 1,
           ttl:     Int  = 0,
           default: Option[BigInt] = None): Option[BigInt] = {
    channel.write(RequestBuilder.incrOrDecr(Ops.Increment, key, count, ttl, default))
    handleResponse(Ops.Increment, handleIncrDecrResponse)
  }

  def decr(key:     Array[Byte],
           count:   Long = 1,
           ttl:     Int  = 0,
           default: Option[BigInt] = None): Option[BigInt] = {
    channel.write(RequestBuilder.incrOrDecr(Ops.Decrement, key, count, ttl, default))
    handleResponse(Ops.Decrement, handleIncrDecrResponse)
  }

  def flush(after: Option[Int] = None): Unit = {
    channel.write(RequestBuilder.flush(after))
    handleResponse(Ops.Flush, handleEmptyResponse)
  }

  def noop: Unit = {
    channel.write(RequestBuilder.noop)
    handleResponse(Ops.NoOp, handleEmptyResponse)
  }

  def get(key: Array[Byte]): Option[T] = {
    channel.write(RequestBuilder.get(key))
    handleResponse(Ops.Get, handleGetResponse)
  }

  def set(key:   Array[Byte],
          value: T,
          ttl:   Int = 0) = {
    val encoded = transcoder.encode(value)
    channel.write(RequestBuilder.storageRequest(Ops.Set, key, encoded.data, encoded.flags, ttl))
    handleResponse(Ops.Set, handleStorageResponse)
  }

  def add(key:   Array[Byte],
          value: T,
          ttl:   Int = 0) = {
    val encoded = transcoder.encode(value)
    channel.write(RequestBuilder.storageRequest(Ops.Add, key, encoded.data, encoded.flags, ttl))
    handleResponse(Ops.Add, handleStorageResponse)
  }

  def replace(key:   Array[Byte],
              value: T,
              ttl:   Int = 0) = {
    val encoded = transcoder.encode(value)
    channel.write(RequestBuilder.storageRequest(Ops.Replace, key, encoded.data, encoded.flags, ttl))
    handleResponse(Ops.Replace, handleStorageResponse)
  }

  def delete(key: Array[Byte]): Boolean = {
    channel.write(RequestBuilder.delete(key))
    handleResponse(Ops.Delete, handleStorageResponse)
  }

  def isConnected: Boolean = {
    channel.isConnected
  }

  def close = {
    try {
      channel.write(RequestBuilder.quit)
      channel.close
    } catch {
      case _ => ()
    }
  }

  private def handleIncrDecrResponse(header: ByteBuffer, body: ByteBuffer): Option[BigInt] = {
    header.getShort(6) match {
      case Status.Success     => Some(BigInt(1, body.array))
      case Status.KeyNotFound => None
      case Status.BadIncrDecr => throw new ProtocolError("Incr/decr on non-numeric value")
      case code               => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleEmptyResponse(header: ByteBuffer, body: ByteBuffer): Unit = {
    header.getShort(6) match {
      case Status.Success  => ()
      case code            => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleStorageResponse(header: ByteBuffer, body: ByteBuffer): Boolean = {
    header.getShort(6) match {
      case Status.Success     => true
      case Status.KeyNotFound => false
      case Status.KeyExists   => false
      case Status.NotStored   => false
      case code               => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleGetResponse(header: ByteBuffer, body: ByteBuffer): Option[T] = {
    header.getShort(6) match {
      case Status.Success => {
        val extrasLen  = header.get(4).toInt
        val encoded    = new EncodedValue(data  = body.array.slice(extrasLen, body.capacity),
                                          flags = body.getInt(0))
        Some(transcoder.decode(encoded))
      }
      case _ => None
    }
  }

  private def handleResponse[A](opcode:  Byte,
                                handler: (ByteBuffer, ByteBuffer) => A): A = {
    fillHeader
    verifyMagic
    verifyOpcode(opcode)
    handler(header, fillBodyFromHeader)
  }

  private def fillHeader: Unit = {
    header.clear
    fill(header, 24)
  }

  private def fillBodyFromHeader: ByteBuffer = {
    val len  = header.getInt(8)
    val body = ByteBuffer.allocate(len)
    fill(body, len)
    body
  }

  private def fill(buffer: ByteBuffer, len: Int): Unit = {
    var read = 0

    while (read < len) {
      read += channel.read(buffer)
    }

    buffer.flip
  }

  private def verifyMagic = {
    header.get(0) match {
      case Packets.Response => ()
      case byte             => {
        throw new ProtocolError("Unexpected header magic 0x%x".format(byte))
      }
    }
  }

  private def verifyOpcode(opcode: Byte) = {
    header.get(1) match {
      case x if x == opcode => ()
      case otherByte        => {
        throw new ProtocolError("Unexpected opcode 0x%x".format(otherByte))
      }
    }
  }
}
