package pl.jaca.server.db

import java.sql.SQLTimeoutException

import pl.jaca.cluster.Configurable
import slick.backend.DatabaseConfig
import slick.driver.MySQLDriver

/**
 * @author Jaca777
 *         Created 2015-12-26 at 18
 */
object DbAccessor extends Configurable {
  /**
   * Creates database connection and returns it.
   */
  val Db = DatabaseConfig.forConfig[MySQLDriver]("server-app.database", appConfig).db

  //Testing connection
  try {
    val conn = Db.source.createConnection()
  } catch {
    case e: SQLTimeoutException =>
      throw new RuntimeException("Unable to create connection with db.", e)
  }
}
