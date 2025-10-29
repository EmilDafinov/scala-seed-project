package example.shortener


import java.net.URL
import java.security.MessageDigest
import java.util.Base64
import scala.util.Random

class HashGenerator(random: Random) {

  val indexToChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    .zipWithIndex
    .map {
      case (char, index) => index -> char
    }
    .toMap

  private val invalidBase64 = Set('+', '=')

  def generateHash(url: URL, attempt: Int = 0): String = {
//    val salt = random.alphanumeric.take(4).mkString
//    val crc = new CRC32
//    crc.update(url.toString.getBytes)
//    val crcHex = crc.getValue.toHexString
    val md5 = MessageDigest.getInstance("MD5")
    md5.update(s"${url.toString}".getBytes)
    val md5Hash = md5.digest()
    val result = new String(
      Base64
        .getEncoder
        .encode(md5Hash)
    )
    .filterNot(invalidBase64.contains)
    .take(7)

    if (attempt > 0) s"$result-$attempt"
    else result
  }
}
