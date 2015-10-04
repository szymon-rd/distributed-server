package pl.jaca.server.cluster.distribution

/**
 * @author Jaca777
 *         Created 2015-10-04 at 18
 */
trait Distributable {
  def getLoad: Load
}
