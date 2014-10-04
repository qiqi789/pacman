package epfl.pacman

import java.awt.Color
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import akka.actor.{ Actor, ActorSystem, Props }
import maze.Controllers._
import maze.Views._
import scala.swing.Button
import scala.swing.Action
import scala.swing.{ MenuBar, Menu, MenuItem }
import scala.swing.event.Key

object Main extends SimpleSwingApplication {

  val mvc = new maze.MVC()

  def top = new MainFrame {
    title = text("titleText")
    background = Color.BLACK
    contents = mvc.gui

    menuBar = new MenuBar {
      contents += new Menu("Menu") {
        mnemonic = Key.F
        contents += new MenuItem(Action("Exit") { mvc.system.shutdown(); dispose(); sys.exit }) { mnemonic = Key.X }
      }
    }

    maximize()
  }

  //mvc.controller.start()
  //Thread.sleep(1000)
  //mvc.ticker.start()

}
