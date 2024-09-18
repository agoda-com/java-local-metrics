package io.agodadev.testmetricsscala

import scala.sys.process._
import scala.util.{Try, Success, Failure}

case class GitContext(repositoryUrl: String, repositoryName: String, branchName: String)

class GitContextException(message: String, cause: Throwable = null) extends Exception(message, cause)

object GitContextReader {
  def getGitContext(): GitContext = {
    val url = runCommand("config --get remote.origin.url")
    val branch = sys.env.getOrElse("CI_COMMIT_REF_NAME", runCommand("rev-parse --abbrev-ref HEAD"))

    if (url.isEmpty) {
      throw new GitContextException("Unable to get git remote url.")
    }
    if (branch.isEmpty) {
      throw new GitContextException("Unable to get git branch.")
    }

    val cleanedUrl = cleanGitlabCIToken(url)
    GitContext(
      repositoryUrl = cleanedUrl,
      repositoryName = getRepositoryNameFromUrl(cleanedUrl),
      branchName = branch
    )
  }

  private def runCommand(args: String): String = {
    val gitCommand = if (sys.props("os.name").toLowerCase.contains("win")) "git.exe" else "git"
    Try(s"$gitCommand $args".!!.trim) match {
      case Success(output) => output
      case Failure(ex) => throw new GitContextException("Failed to run git command.", ex)
    }
  }

  private def getRepositoryNameFromUrl(url: String): String = {
    val repositoryName = url.split('/').last
    if (repositoryName.endsWith(".git")) repositoryName.dropRight(4) else repositoryName
  }

  private def cleanGitlabCIToken(url: String): String = {
    if (url.contains("@") && url.startsWith("https")) {
      s"https://${url.split('@').last}"
    } else url
  }
}