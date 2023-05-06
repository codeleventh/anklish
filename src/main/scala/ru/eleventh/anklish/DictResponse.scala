package ru.eleventh.anklish

import io.circe.generic.JsonCodec

@JsonCodec
final case class DictResponse(word: String, phonetics: List[Phonetic], meanings: List[Meaning])

@JsonCodec
final case class Phonetic(text: Option[String])

@JsonCodec
final case class Meaning(partOfSpeech: Option[String], definitions: List[Definition])

@JsonCodec
final case class Definition(
    definition: Option[String],
    example: Option[String],
    synonyms: List[String]
)
