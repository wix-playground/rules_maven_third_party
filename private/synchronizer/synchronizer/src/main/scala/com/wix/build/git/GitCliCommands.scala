package com.wix.build.git

import com.wix.build.git.GitCliCommands.GetFullLogDefaultValue

import java.nio.file.Path
import scala.sys.process.{Process, ProcessLogger}

trait GitCliCommands {

  def commandsBaseDir: Path

  var constantEnvironmentVariables: List[(String, String)] = List()

  private def appendLine(stringBuilder: StringBuilder)(line: String): Unit = stringBuilder.append(s"$line\n")

  def runProcess(command: String, logPrefix: String, envVarList: List[(String, String)] = List(), getFullLog: Boolean = GetFullLogDefaultValue): String = {
    val stdOutErrBuilder = new StringBuilder
    val append: String => Unit = appendLine(stdOutErrBuilder)
    val envVars = constantEnvironmentVariables ++ envVarList
    var processOutput: String = s"""Running "$command" failed to complete"""
    def fullLog(): String = processOutputsTemplate(logPrefix, processOutput, stdOutErrBuilder.toString())

    println(s"[$logPrefix][Run] $command")
    try {
      processOutput = envVars match {
        case vars if vars.isEmpty => Process(command).!!(ProcessLogger(append))
        case vars => Process(command, None, vars:_*).!!(ProcessLogger(append))
      }
    } finally {
        println(fullLog())
    }
    if (getFullLog) fullLog()
    else processOutput
  }

  def processOutputsTemplate(logPrefix: String, output: String, stdOutErr: String): String = {
    val outputLine =
      if (output.isEmpty || output.last != '\n') output + "\n"
      else output
    s"""[$logPrefix][Output] $outputLine[$logPrefix][stdout/err] $stdOutErr"""
  }

  def runGitCommand(command: String, getFullLog: Boolean = GetFullLogDefaultValue): String = {
    val fullCommand = s"git -C $commandsBaseDir $command "
    runProcess(fullCommand, "GitCliCommands", getFullLog = getFullLog)
  }
}

object GitCliCommands {
  val GetFullLogDefaultValue = false
}

trait GitCheckout extends GitCliCommands {
  def checkout(branch: String, isNewBranch: Boolean = false, getFullLog: Boolean = GetFullLogDefaultValue): String = {
    val prefix = Seq("checkout")
    val postfix = Seq(branch)
    val command = if (isNewBranch) {
      prefix ++ Seq("-b") ++ postfix
    } else {
      prefix ++ postfix
    }
    runGitCommand(command.mkString(" "), getFullLog)
  }
}

trait GitAddAll extends GitCliCommands {
  def addAll(): Unit = runGitCommand("add .")
}

trait GitCommit extends GitCliCommands {
  def commit(message: String): Unit = {
    runGitCommand(s"""commit -m "$message"""")
  }
}


trait GitAdd extends GitCliCommands {
  def addFiles(fileSet: Set[String]): Unit = runGitCommand(s"add ${fileSet.mkString(" ")}", getFullLog = true)
}

class GitAdder(rootDirectory: Path) extends GitAdd {
  override def commandsBaseDir: Path = rootDirectory
}

