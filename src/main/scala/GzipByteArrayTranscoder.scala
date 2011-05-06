package com.bitlove.memcached

import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class GzipByteArrayTranscoder(minSizeToCompress: Int = 1000)
      extends Transcoder[Array[Byte]] {
  val CompressedFlag = 2

  def encode(value: Array[Byte]): EncodedValue = {
    if (value.size >= minSizeToCompress) {
      val baos = new ByteArrayOutputStream
      val gzos = new GZIPOutputStream(baos)
      gzos.write(value)
      gzos.finish
      gzos.close
      baos.close
      new EncodedValue(data = baos.toByteArray, flags = CompressedFlag)
    } else {
      new EncodedValue(data = value, flags = 0)
    }
  }

  def decode(encoded: EncodedValue): Array[Byte] = {
    if (encoded.flags == CompressedFlag) {
      val bais   = new ByteArrayInputStream(encoded.data)
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
    } else {
      encoded.data
    }
  }
}
