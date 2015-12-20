package pl.jaca.server.packets

/**
 * @author Jaca777
 *         Created 2015-12-17 at 19
 */
trait DataConstructor {
/*  protected def constructData(values: AnyRef*) = {
    def constructAcc(acc: Array[Byte], values: AnyRef*) = {
      val elem = values.head match {
        case b: Boolean => Array(if (b) 1.toByte else 0.toByte)
        case i: Int => Array(i.to)
      }
      constructAcc(acc ++ elem, values.tail)
    }
    constructAcc(Array.empty, values)
  }*/
}
