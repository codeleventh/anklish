package ru.eleventh.anklish.model

import io.circe.generic.JsonCodec

@JsonCodec
final case class DictResponse(word: String, phonetic: Option[String], meanings: List[Meaning])

@JsonCodec
final case class Meaning(partOfSpeech: String, definitions: List[Definition])

@JsonCodec
final case class Definition(
    definition: String,
    example: Option[String],
    synonyms: List[String],
    antonyms: List[String]
)
