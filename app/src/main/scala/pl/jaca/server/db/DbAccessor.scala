package pl.jaca.server.db

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
  lazy val Db = DatabaseConfig.forConfig[MySQLDriver]("server-app.database", appConfig).db
}
