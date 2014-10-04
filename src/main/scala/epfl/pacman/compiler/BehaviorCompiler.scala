package epfl.pacman
package compiler

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.reporters.AbstractReporter
import java.lang.String
import scala.tools.nsc.util.{ Position => FilePosition, BatchSourceFile }
import collection.mutable.{ Set, HashSet }
import maze.MVC
import behaviour.Behavior

//my edition
import maze.Controllers._

abstract class BehaviorCompiler {

  val mvc: MVC

  private val template =
    """package epfl.pacman
package behaviour

import maze.MVC

object Factory {
  // could use dependent method type
  def create(): Behavior = new Behavior {
    //val mvc = theMVC
    //import mvc._
    
      //my edit
  import maze.Models._
  import maze.Thingies._
    
    def getMethod(model: Model, p: Figure) = {
      new NextMethod(model, p) {
        def apply = {
          None
%s
        }
      }
    }
  }
}"""

  // number of lines before user's text
  private val errorOffset = 14

  private val settings = new Settings()

  settings.classpath.value = {
    def pathOf(cls: Class[_]) = {
      val l = cls.getProtectionDomain.getCodeSource.getLocation
      new java.io.File(l.toURI).getAbsolutePath
    }
    import java.io.File.{ pathSeparator => % }
    pathOf(this.getClass) + % + pathOf(classOf[ScalaObject]) + % + pathOf(classOf[Global]) + % + pathOf(classOf[scala.swing.Dialog])
  }

  private val outDir = new VirtualDirectory("(memory)", None)
  settings.outputDirs.setSingleOutput(outDir)

  private val reporter = new AbstractReporter {
    val settings = BehaviorCompiler.this.settings

    val errorPositions: Set[FilePosition] = new HashSet()

    override def reset {
      errorPositions.clear()
      super.reset
    }

    def display(pos: FilePosition, msg: String, severity: Severity) {
      println(msg)
      printSourceLine(pos)
      severity.count += 1
      if (severity == ERROR)
        errorPositions += pos
    }

    def printSourceLine(pos: FilePosition) {
      println(pos.lineContent.stripLineEnd)
      printColumnMarker(pos)
    }

    def printColumnMarker(pos: FilePosition) =
      if (pos.isDefined) { println(" " * (pos.column - 1) + "^") }

    def displayPrompt {
      fatal()
    }

    def fatal() {

    }
  } // private val reporter

  private val global = new Global(settings, reporter)

  private def columnToLine(column: Int, lengths: List[Int]) = {
    val r: (Int, Int) = ((0, 1) /: lengths)((sumLine, lineLength) => {
      val sum = sumLine._1 + lineLength
      if (column > sum)
        (sum, sumLine._2 + 1)
      else
        (sum, sumLine._2)
    })
    r._2
  }

  def compile(body: String) {
//    val t = new Thread() {
//      override def run() {
    val t = new Runnable {
      def run() {

        val (line, lengths) = (body.split("\n") :\ (("", List[Int]())))((line, acc) => (line + " " + acc._1, (line.length + 1) :: acc._2))

        val source = template.format(line)
        val run = new global.Run
        val file = new BatchSourceFile("<behavior>", source)
        run.compileSources(List(file))

        if (reporter.hasErrors) {
          val errorLines = reporter.errorPositions.map(pos => columnToLine(pos.column, lengths)) // (_.line - errorOffset)
          val text = errorLines.mkString("erroneous line(s): ", ", ", "")
          println(text)
          swing.Swing.onEDT {
            mvc.controller ! FoundErrors(errorLines)
          }
        } else {
          val parent = this.getClass.getClassLoader
          val classLoader = new AbstractFileClassLoader(outDir, parent)

          val mvcClass = classOf[MVC]

          val behavior = classLoader.findClass("epfl.pacman.behaviour.Factory")
          //val m = behavior.getMethod("create", mvcClass)
          // the getMethod is a different one from the same name method in Factory.
          val m = behavior.getMethod("create")
          val behaviorInst = m.invoke(null).asInstanceOf[Behavior { }]//val mvc: BehaviorCompiler.this.mvc.type }]

          swing.Swing.onEDT {
            mvc.controller ! Load(behaviorInst)
          }
        }
        reporter.reset
        
      } // override def run()
    } // new Thread()
    //t.start()
    val tt = new Thread(t)
    tt.start()
  }

}
