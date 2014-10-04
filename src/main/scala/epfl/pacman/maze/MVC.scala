package epfl.pacman
package maze

import interfac._

import compiler.BehaviorCompiler

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorSystem

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

/*
GENERAL COMPOSITION STRUCTURE

trait Models extends Thingies with Positions with Directions { this: MVC =>
  class Model
}

trait Thingies { this: Models => }
trait Positions { this: Models => }
trait Directions { this: Models => }

trait Views { this: MVC =>
  class View
}

trait Controllers { this: MVC =>
  class Controller {
    var b = new Behavior { val mvc: Controllers.this.type = Controllers.this }
    def go { b.next(model) }
    def load(l: Load) { b = l.nb }
  }

  case class Load(nb: Behavior { val mvc: Controllers.this.type })
}

trait MVC extends Models with Views with Controllers {
  val model = new Model
  val view = new View
  val controller = new Controller
}

abstract class Behavior {
  val mvc: MVC
  def next(m: mvc.Model) = 1
}

abstract class BehaviorCompiler {
  val mvc: MVC
  def compile {
    val b = new Object
    mvc.controller.load(mvc.Load(b.asInstanceOf[Behavior { val mvc: BehaviorCompiler.this.mvc.type } ]))
  }
}

*/

//my edition
import Models._
import Views._
import Controllers._
import interfac.GUIs._
import epfl.pacman.model

class MVC { //extends Models with Views with GUIs with Sounds with Controllers {
  //val lang = "fr"
  //  val lang = "en"
  //  val locale = new java.util.Locale(lang)

  //  val messages = java.util.ResourceBundle.getBundle("UI", locale)
  //  def text(key: String) = messages.getString(key)

  // this is a var, its changes need to be seen by others.
  model = new Model()
  val view = new View()

  // mvc's components creation order matters
  // This way only works for solid components like model,view,gui to interact with controller actor, not among themselves.
  val gui = new PacmanScreen(view){val mvc: MVC.this.type = MVC.this}

  // because BehaviorCompiler is an abstract class, it can have abstract val mvc.
  // but abstract class can not be instantiated directly.
  val compiler = new BehaviorCompiler {
    val mvc: MVC.this.type = MVC.this
  }

  val system = ActorSystem("packman")

  val controller = system.actorOf(Controller.props(MVC.this) , "pacman-controller")

  val tickerActor = system.actorOf(ticker.props(controller), name = "pacman-ticker")

  //system.scheduler.scheduleOnce(1.seconds, tickerActor, Tick)

   

}
