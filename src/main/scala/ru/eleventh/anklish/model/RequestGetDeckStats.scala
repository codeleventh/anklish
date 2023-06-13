package ru.eleventh.anklish.model

import io.circe.generic.JsonCodec
import ru.eleventh.anklish.model.AnkiActionEnum.AnkiActionEnum

@JsonCodec
case class ParamsGetDeckStats(decks: Seq[String])

@JsonCodec
case class DeckStat(
    deck_id: Long,
    name: String,
    new_count: Int,
    learn_count: Int,
    review_count: Int,
    total_in_deck: Int
) {}

@JsonCodec
case class RequestGetDeckStats(
    params: ParamsGetDeckStats,
    action: AnkiActionEnum = AnkiActionEnum.GetDeckStats
) extends AnkiRequest[ParamsGetDeckStats](params, action)
