package helpers

import javax.inject.Singleton

/**
  * Created by fincash on 06-04-2017.
  */

@Singleton
class AMCHelper {

  def isISIPAllowed(amctrfnum:Long, amctList:Array[String]): Boolean ={
    var allowed = false
    for(i <- 0 until amctList.length){
      if(amctList(i).toLong == amctrfnum){
        allowed = true
      }
    }
    allowed
  }
}
