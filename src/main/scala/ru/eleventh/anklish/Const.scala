package ru.eleventh.anklish

import org.http4s.implicits.http4sLiteralsSyntax

import scala.concurrent.duration.{DurationDouble, FiniteDuration}

object Const {
  val NET_DICT_API_URL = uri"https://api.dictionaryapi.dev/api/v2/entries/en"
  val NET_RETRIES = 3
  val NET_DELAY: FiniteDuration = 1.second

  val ARG_MAX_CARDS = 10
  val ARG_ANKI_PATH = "anki"

  val ANKI_CONNECT_URL = "http://localhost:8765"
  val ANKI_CONNECT_VERSION = 6
}
