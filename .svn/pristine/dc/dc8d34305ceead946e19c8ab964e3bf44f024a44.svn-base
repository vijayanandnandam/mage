package service

import java.io.File
import java.util.Calendar

import play.api.libs.Files.TemporaryFile
import scala.io.Source
import play.api.mvc.MultipartFormData
import play.api.libs.Files
import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

class AttachmentService {

  def DEFAULTPATH: String = "E:\\fincash\\data\\mailEngine\\attachments\\"

  /**
   *
   * Used to save the attachment in file system as per the provided base path
   * @param tempFile
   * @return
   */
  def saveFile(files: Seq[MultipartFormData.FilePart[Files.TemporaryFile]]): HashMap[String, String] = {

    var fileNames = new HashMap[String, String];
    for (file <- files) {
      val regex = new Regex("""[$#%^&*]""");
      var name = regex.replaceAllIn(file.filename, "_");
      name = Calendar.getInstance.getTimeInMillis + "_" + name
      val path = DEFAULTPATH + name
      file.ref.moveTo(new File(path))
      fileNames += (file.filename -> path)
    }
    return fileNames
  }

  /**
   *
   * This API is used to read attachment files from the system as per the path provided
   * @param path
   * @param fileName
   */
  def readFile(path: String, fileName: String): String = {

    val source = Source.fromFile(path + "\\" + fileName)
    val lines = try source.mkString finally source.close()
    return lines
  }

  /**
   *
   * This API is used to read file from the default system path
   * @param fileName
   */
  def readFile(fileName: String): Unit = {
    readFile(DEFAULTPATH, fileName)
  }

}