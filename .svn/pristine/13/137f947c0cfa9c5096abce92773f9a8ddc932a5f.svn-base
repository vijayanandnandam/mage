package repository.core

import scala.reflect._
import slick.jdbc.MySQLProfile.api._
/**
 * Created by sagar on 16/1/17.
 */
trait BaseEntity {
  val id: Long
}

abstract class BaseTable[E: ClassTag](tag: Tag, tableName: String) extends Table[E](tag, tableName) {
  val classOfEntity = classTag[E].runtimeClass
  val id: Rep[Long] = column[Long]("Id", O.AutoInc, O.PrimaryKey)
  /** Database column CREATEDATE SqlType(TIMESTAMP), Default(None) */
  val createdate: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("CREATEDATE", O.Default(None))
  /** Database column MODIFYDATE SqlType(TIMESTAMP) */
  val modifydate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("MODIFYDATE")
  /** Database column CREATEDBY SqlType(CHAR), Length(64,false) */
  val createdby: Rep[String] = column[String]("CREATEDBY", O.Length(64, varying = false))
  /** Database column LASTMODIFIEDBY SqlType(CHAR), Length(64,false) */
  val lastmodifiedby: Rep[String] = column[String]("LASTMODIFIEDBY", O.Length(64, varying = false))
}



