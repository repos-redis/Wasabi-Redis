package org.wasabiredis

import actors.Actor
import redis.clients.jedis.JedisPubSub

/**
 * Demultiplexing subscriber.
 * Sends all incoming messages to a routing actor.
 */

sealed class FunThread(mainLoop: () => Unit) extends Thread {
  override def run() {
    mainLoop()
  }
}

class WasabiSubscriber(val redisHost: String = "localhost", val redisPort: Int = 6379) extends WithJedis {

  private lazy val pubSub = new JedisPubSub() {
    def onMessage(channel: String, message: String) {
      dispatcherActor ! OnMessage(channel, message)
    }
    def onPMessage(pattern: String, channel: String, message: String) {

    }
    def onPSubscribe(pattern: String, subscribedChannels: Int) {

    }
    def onPUnsubscribe(pattern: String, subscribedChannels: Int) {

    }
    def onSubscribe(channel: String, subscribedChannels: Int) {

    }
    def onUnsubscribe(channel: String, subscribedChannels: Int) {

    }
  }

  private var clientThread: Option[FunThread] = None

  private val dispatcherActor: Actor = new Actor {
    def act() {
      loop {
        receive {
          case m: OnMessage => listeningStation.handlersFor(m.channel).foreach(_(m))
        }
      }
    }
  }

  private val listeningStation = new ListeningStation()

  def start(channel: String) {
    dispatcherActor.start()

    clientThread = Some(new FunThread(() => jedis.subscribe(pubSub, channel)))
    clientThread.foreach(_.start())
  }

  def stop() {
    pubSub.unsubscribe(listeningStation.channels.toArray: _*)
  }

  def <+>(tuple: (String, OnMessage => Unit)) {
    listeningStation <+> tuple
  }
}

/**
 * The main data type here - that's what you get for messages and such.
 */
case class OnMessage(channel: String, message: String)

/**
 * Completely not thread-safe at the moment.
 * Oops.
 */
sealed class ListeningStation {

  import scala.collection.mutable._

  private val routeMap = HashMap[String, ListBuffer[OnMessage => Unit]]()

  def <+>(t: (String, OnMessage => Unit)) {
    routeMap.get(t._1) match {
      case Some(listBuffer) => listBuffer += t._2
      case None => routeMap += t._1 -> ListBuffer(t._2)
    }
  }

  def channels = routeMap.keySet
  def handlersFor(channel: String): List[OnMessage => Unit] = routeMap.get(channel).map(_.toList).getOrElse(List())
}