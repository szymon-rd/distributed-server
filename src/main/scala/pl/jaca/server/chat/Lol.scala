package pl.jaca.server.chat

import akka.actor.Actor
import pl.jaca.server.cluster.distribution.{AbsoluteLoad, Load, Distributable, Distribution}
/**
 * @author Jaca777
 *         Created 2015-06-11 at 21
 */
class Lol extends Actor with Distribution{
  println("lol")
  context.distribute(new Lol2(22))
  def receive: Receive = {
    case _ => context.stop(self)
  }

}
class Lol2(val i: Int) extends Actor with Distributable{
  println("WORKING " + i)
  override def receive: Actor.Receive = {
    case _ =>
  }

  override def getLoad: Load = AbsoluteLoad(1.0f)
}
