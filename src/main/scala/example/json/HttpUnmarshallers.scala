package example.json

import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshaller

import java.net.URL

object HttpUnmarshallers {
  implicit val urlUnmarshaller: Unmarshaller[String, URL] = Unmarshaller.identityUnmarshaller[String].map(new URL(_))
}
