package epfl.pacman

object test {
  import akka.actor.Actor
  import akka.actor.Props
  import akka.actor.ActorRef
  import akka.actor.ActorLogging
  import akka.actor.ActorSystem

  case class Greeting(who: String)

  class GreetingActor extends Actor with ActorLogging {
    def receive = {
      case Greeting(who) => log.info("Hello " + who)
    }
  }

  def main(args: Array[String]) = {
    val system = ActorSystem("MySystem")
    val greeter = system.actorOf(Props[GreetingActor], name = "greeter")
    greeter ! Greeting("Charlie Parker")
  }

}