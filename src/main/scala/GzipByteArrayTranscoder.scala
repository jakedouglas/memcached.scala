package com.bitlove.memcached

import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class GzipByteArrayTranscoder[T](minSizeToCompress: Int = 1000,
                                 next: Transcoder[Array[Byte], T] = new ByteArrayTranscoder)
      extends Transcoder[Array[Byte], T] {

  private val CompressedFlag = 2

  def encode(value: Transcodable[Array[Byte]]): Transcodable[T] = {
    val encoded = (value.data.size >= minSizeToCompress) match {
      case true  => new Transcodable(data = compress(value.data), flags = value.flags | CompressedFlag)
      case false => value
    }

    next.encode(encoded)
  }

  def decode(encoded: Transcodable[T]): Transcodable[Array[Byte]] = {
    val decoded = next.decode(encoded)

    (encoded.flags & CompressedFlag) match {
      case 0 => decoded
      case _ => new Transcodable(data = uncompress(decoded.data), flags = encoded.flags)
    }
  }

  private def compress(data: Array[Byte]): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val gzos = new GZIPOutputStream(baos)
    gzos.write(data)
    gzos.finish
    gzos.close
    baos.close
    baos.toByteArray
  }

  private def uncompress(data: Array[Byte]): Array[Byte] = {
    val bais   = new ByteArrayInputStream(data)
    val gzis   = new GZIPInputStream(bais)
    val baos   = new ByteArrayOutputStream
    val buf    = new Array[Byte](10000)
    var read   = gzis.read(buf)

    while (read > 0) {
      baos.write(buf, 0, read)
      read = gzis.read(buf)
    }

    gzis.close
    baos.close
    baos.toByteArray
  }
}
