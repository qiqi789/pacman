package epfl.pacman

import java.awt.Color
import javax.swing.JApplet

class MainApplet extends JApplet {

  this.setBackground(Color.BLACK)

  val mvc = new maze.MVC

  this.add(mvc.gui.peer)

  //mvc.controller.start()
  Thread.sleep(1000)
  //mvc.ticker.start()

}