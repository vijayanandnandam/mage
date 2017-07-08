package models

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONLong, BSONObjectID, BSONString}

case class Token(_id: BSONObjectID, token: String, username: String, key: String, algo: String, expiry: Long)
/**
  * Created by Fincash on 19-01-2017.
  */
object Token {
  implicit object TokenReader extends BSONDocumentReader[Token] {
    def read(doc: BSONDocument): Token = {
      val id = doc.getAs[BSONObjectID]("_id").get
      val token = doc.getAs[BSONString]("token").get.value
      val username = doc.getAs[BSONString]("username").get.value
      val key = doc.getAs[BSONString]("key").get.value
      val algo = doc.getAs[BSONString]("algo").get.value
      val expiry = doc.getAs[BSONLong]("expiry").get.value

      Token(id, token, username, key, algo, expiry);
    }
  }

  implicit object TokenWriter extends BSONDocumentWriter[Token] {
    def write(tokenObj: Token) = {
      val doc = BSONDocument(
        "token" -> BSONString(tokenObj.token),
        "username" -> BSONString(tokenObj.username),
        "key" -> BSONString(tokenObj.key),
        "algo" -> BSONString(tokenObj.key),
        "expiry" -> BSONLong(tokenObj.expiry)
      )
      doc
    }
  }
}