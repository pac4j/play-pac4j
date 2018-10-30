package org.pac4j.play.filters

import akka.Done
import play.api

import scala.collection.mutable
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag

private class MockInMemoryAsyncCacheApi() extends api.cache.AsyncCacheApi {
  private val map: mutable.Map[String, Any] = mutable.HashMap()

  override def set(key: String, value: Any, expiration: Duration): Future[Done] = Future.successful {
    map(key) = value
    Done
  }

  override def remove(key: String): Future[Done] = Future.successful {
    map -= key
    Done
  }

  override def getOrElseUpdate[A](key: String, expiration: Duration)(orElse: => Future[A])(implicit evidence$1: ClassTag[A]): Future[A] = Future.successful {
    map.getOrElseUpdate(key, Await.result(orElse, 2.seconds)).asInstanceOf[A]
  }

  override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Future[Option[T]] = Future.successful {
    map.get(key).map(_.asInstanceOf[T])
  }

  override def removeAll(): Future[Done] = Future.successful {
    map.clear()
    Done
  }
}
