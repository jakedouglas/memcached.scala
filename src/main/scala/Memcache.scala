import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.SocketChannel
import java.net.InetSocketAddress

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

object Packets {
  val Request  = 0x80.toByte
  val Response = 0x81.toByte
}

object Ops {
  val Get = 0x00.toByte
  val Set = 0x01.toByte
}

object Status {
  val Success     = 0x0.toShort
  val KeyNotFound = 0x1.toShort
  val KeyExists   = 0x2.toShort
  val TooLarge    = 0x3.toShort
  val InvalidArgs = 0x4.toShort
  val NotStored   = 0x5.toShort
  val BadIncrDecr = 0x6.toShort
}

object RequestBuilder {
  def get(key: Array[Byte]): Array[ByteBuffer] = {
    val reqBuf = ByteBuffer.allocate(24)

    reqBuf.put      (0, Packets.Request)
    reqBuf.put      (1, Ops.Get)
    reqBuf.putShort (2, key.size.toShort)
    reqBuf.putInt   (8, key.size)

    Array(reqBuf, ByteBuffer.wrap(key))
  }

  def set(key:   Array[Byte],
          value: Array[Byte],
          flags: Int,
          ttl:   Int): Array[ByteBuffer] = {
    val reqBuf = ByteBuffer.allocate(32)

    reqBuf.put      (0,  Packets.Request)
    reqBuf.put      (1,  Ops.Set)
    reqBuf.putShort (2,  key.size.toShort)
    reqBuf.put      (4,  8.toByte)
    reqBuf.putInt   (8,  key.size + value.size + 8)
    reqBuf.putInt   (24, flags)
    reqBuf.putInt   (28, ttl)

    Array(reqBuf,
          ByteBuffer.wrap(key),
          ByteBuffer.wrap(value))
  }
}

class Memcached(host: String, port: Int) {
  class ProtocolError(message: String) extends Error(message)

  private val addr            = new InetSocketAddress(host, port)
  private val channel         = SocketChannel.open(addr)
  private val header          = ByteBuffer.allocate(24)

  def get(key: Array[Byte]): Option[Array[Byte]] = {
    channel.write(RequestBuilder.get(key))
    handleResponse(Ops.Get, handleGetResponse)
  }

  def set(key:   Array[Byte],
          value: Array[Byte],
          ttl:   Int = 0,
          flags: Int = 0) = {
    channel.write(RequestBuilder.set(key, value, flags, ttl))
    handleResponse(Ops.Set, handleSetResponse)
  }

  private def handleSetResponse(header: ByteBuffer): Unit = {
    val extras  = header.get(4).toInt
    val dataLen = header.getInt(8)
    val dataBuf = ByteBuffer.allocate(dataLen)
    fill(dataBuf, dataLen)

    header.getShort(6) match {
      case Status.Success => ()
      case code           => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleGetResponse(header: ByteBuffer): Option[Array[Byte]] = {
    val extras  = header.get(4).toInt
    val dataLen = header.getInt(8)
    val dataBuf = ByteBuffer.allocate(dataLen)
    fill(dataBuf, dataLen)

    header.getShort(6) match {
      case Status.Success => {
        Some(dataBuf.array.slice(extras, dataLen))
      }
      case _              => None
    }
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

  private def handleResponse[T](opcode:  Byte,
                                handler: ByteBuffer => T): T = {
    header.clear
    fill(header, 24)
    verifyMagic
    verifyOpcode(opcode)
    handler(header)
  }

  private def fill(buffer: ByteBuffer, len: Int): Unit = {
    var read = 0

    while (read < len) {
      read += channel.read(buffer)
    }
  }
}

object Memcache {
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