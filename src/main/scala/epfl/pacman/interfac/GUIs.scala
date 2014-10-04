package epfl.pacman
package interfac

import swing._
import Swing._
import event.{ ButtonClicked, ValueChanged }
import maze.MVC
import editor.ScalaPane
import java.awt.{ Font, Color, Insets }


/**
 * @TODO actually, not the whole GUI needs to be part of the MVC, only the components that
 * need to be updated / notified by the controller should be part of the view. the layout,
 * the text area etc could be kept outside the MVC.
 */

//my edition
object GUIs {
    //my edition
    import maze.Views._
    import maze.Controllers._
    import maze.Models._
    import akka.actor.ActorRef
    import epfl.pacman.model
    
abstract class PacmanScreen(view: View) extends GridBagPanel {
    import GridBagPanel._

    // abstract MVC world
    val mvc: MVC
    //var controller: ActorRef = null

    // not used right now, but this is what they should be...
    //  val width = 10 + Settings.docTextWidth + 10 + Settings.codeTextWidth + 10 + view.width + 10
    //  val height = 10 + view.height + 10

    val code = new ScalaPane()
    code.background = Color.BLACK
    code.peer.setCaretColor(Color.WHITE)
    code.keywords ++= Settings.keywords
    code.preferredSize = (Settings.codeTextWidth, 0)
    //code.notifyUpdate() // should highlight existing text, doesnt' work..

    val runButton = new Button(text("loadButton"))

    val simpleMode = new RadioButton(text("simpleButton"))
    simpleMode.background = Color.BLACK // for windows
    simpleMode.foreground = Color.WHITE
    simpleMode.selected = true
    val advancedMode = new RadioButton(text("advancedButton"))
    advancedMode.background = Color.BLACK // for windows
    advancedMode.foreground = Color.WHITE
    val modeGroup = new ButtonGroup(simpleMode, advancedMode)

    val pauseButton = new Button("")

    val resetButton = new Button(text("restartButton"))
    
    //my edit
    val exitButton = new Button(text("exitButton"))

    val statusTitle = new Label("")
    statusTitle.foreground = Color.WHITE
    statusTitle.font = new Font(statusTitle.font.getName, Font.BOLD, 14)

    val scoreTitle = new Label("")

    scoreTitle.foreground = Color.GREEN
    scoreTitle.font = new Font(scoreTitle.font.getName, Font.BOLD, 14)

    val statusDisplay = new Component {
      preferredSize = (200, 10)

      override def paintComponent(g: Graphics2D) {
        import java.awt.RenderingHints.{ KEY_ANTIALIASING, VALUE_ANTIALIAS_ON }
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)

        if (model.simpleMode) {
          val angle = model.counters('time) * 360 / Settings.surviveTime - 360
          val rb = model.counters('time) * 255 / Settings.surviveTime
          val cv = 0x00ff00 | (rb << 16) | rb
          g.setColor(new Color(cv))
          g.fillArc(50, 20, 100, 100, 90, angle)
        } else {
          g.setColor(Color.YELLOW)
          var y = 20
          for (i <- 0 until model.pacman.lives) {
            g.fillArc(80, y, 40, 40, 30, 300)
            y += 60
          }
        }
      }
    }

    background = Color.BLACK // for windows
    val c = new Constraints

    val left = new GridBagPanel {
      background = Color.BLACK // for windows
      val c = new Constraints

      c.fill = Fill.Horizontal
      c.gridx = 0
      c.gridy = 0
      c.insets = new Insets(10, 10, 15, 10)
      val codeLabel = new Label(text("codeTextfieldTitle"))
      codeLabel.xAlignment = Alignment.Left
      codeLabel.foreground = Color.WHITE
      codeLabel.font = new Font(codeLabel.font.getName, Font.BOLD, 18)
      layout(codeLabel) = c

      c.fill = Fill.Vertical
      c.gridx = 0
      c.gridy = 1
      c.weighty = 1.0
      c.insets = new Insets(5, 10, 5, 10) // top, left, bottom, right
      layout(code) = c

      c.fill = Fill.None
      c.gridx = 0
      c.gridy = 2
      c.weighty = 0.0
      c.insets = new Insets(5, 10, 10, 10)
      layout(runButton) = c
    }
    left.border = new javax.swing.border.LineBorder(Color.GRAY)

    c.fill = Fill.Vertical
    c.gridx = 0
    c.gridy = 0
    c.insets = new Insets(10, 10, 10, 10)
    layout(left) = c

    c.fill = Fill.None
    c.gridx = 1
    c.gridy = 0
    c.insets = new Insets(10, 10, 10, 10)
    layout(view) = c

    val right = new GridBagPanel {
      background = Color.BLACK // for windows
      val c = new Constraints

      c.fill = Fill.Horizontal

      c.gridx = 0
      c.gridy = 1
      c.insets = new Insets(5, 10, 5, 10)
      layout(simpleMode) = c

      c.gridx = 0
      c.gridy = 2
      c.insets = new Insets(0, 10, 10, 10)
      layout(advancedMode) = c

      c.gridx = 0
      c.gridy = 3
      c.insets = new Insets(10, 10, 5, 10)
      layout(pauseButton) = c

      c.gridx = 0
      c.gridy = 4
      c.insets = new Insets(5, 10, 10, 10)
      layout(resetButton) = c

      c.gridx = 0
      c.gridy = 5
      c.insets = new Insets(30, 10, 10, 10)
      layout(scoreTitle) = c

      c.gridx = 0
      c.gridy = 6
      c.insets = new Insets(10, 10, 10, 10)
      layout(statusTitle) = c

      c.fill = Fill.Both
      c.gridx = 0
      c.gridy = 7
      c.weighty = 1.0
      c.insets = new Insets(10, 10, 10, 10)
      layout(statusDisplay) = c
      
      // my edit
      c.fill = Fill.None
      c.gridx = 0
      c.gridy = 8
      c.weighty = 0.0
      c.insets = new Insets(5, 10, 10, 10)
      layout(exitButton) = c
      
    }
    right.border = new javax.swing.border.LineBorder(Color.GRAY)

    c.fill = Fill.Vertical
    c.gridx = 2
    c.gridy = 0
    c.insets = new Insets(10, 10, 10, 10)
    layout(right) = c

    listenTo(runButton, pauseButton, resetButton, simpleMode, advancedMode, exitButton)
    reactions += {
      case ButtonClicked(`runButton`) =>
        code.requestFocus()
        mvc.controller ! Compile(code.text)

      case ButtonClicked(`pauseButton`) =>
        mvc.controller ! Pause

      case ButtonClicked(`resetButton`) =>
        mvc.controller ! Reset(simpleMode.selected)

      case ButtonClicked(`simpleMode`) | ButtonClicked(`advancedMode`) =>
        if (model.simpleMode != simpleMode.selected)
          mvc.controller ! Reset(simpleMode.selected)
          
      case ButtonClicked(`exitButton`) => 
        mvc.system.shutdown()
        System.exit(0)
    }

    def update() {
      onEDT {
        view.repaint()
        statusDisplay.repaint()

        val locked = model.state match {
          case Loading(_) => false
          case _ => true
        }

        runButton.enabled = locked

        pauseButton.text =
          if (model.state == Paused) text("continueButton")
          else text("stopButton")
        pauseButton.enabled = locked

        resetButton.enabled = locked

        if (model.simpleMode) {
          simpleMode.selected = true
          statusTitle.text = text("remainingTimeText")
          scoreTitle.text = ""
        } else {
          advancedMode.selected = true
          statusTitle.text = text("remainingLifesText")
          scoreTitle.text = text("scoreText") + ": " + model.counters('score);
        }

        scoreTitle.repaint()

        simpleMode.enabled = locked
        advancedMode.enabled = locked
      }
    }

    def setErrors(errorLines: collection.Set[Int]) {
      val lines = code.lines
      for ((line, i) <- lines.zipWithIndex) {
        // i is 0-based, line numbers start at 1
        if (errorLines contains (i + 1))
          line.highlight
      }
    }

    update()

  }

}

//trait GUIs { this: MVC =>
//
//}


