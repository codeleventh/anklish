package ru.eleventh.anklish

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  def run: IO[Unit] = AnklishServer.run
}
