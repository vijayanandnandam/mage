package controllers

import com.google.inject.Inject
import constants.MongoConstants
import org.slf4j.LoggerFactory
import play.api.mvc.{Action, Controller}
import service.{MongoDbService, SchemeService}

import scala.concurrent.ExecutionContext

class WishlistController @Inject() (implicit ec: ExecutionContext, schemeService: SchemeService, mongoDbService: MongoDbService)
    extends Controller with MongoConstants {
  val logger, log = LoggerFactory.getLogger(classOf[WishlistController])


  def collection(name: String) =  mongoDbService.collection(name)

  /**
    * This is dummy method used to add funds to solr index
    * @return
    */
  def addToWishlist = Action(parse.json) { request =>
   /* val data = request.body.as[WishlistItem];
    val wishlistCollection = collection(WISHLIST_COLLECTION_NAME);
    wishlistCollection.flatMap(collection =>
      collection.insert(data).map(wr =>
        if (wr.hasErrors)
          InternalServerError("Some server error has occured please try again later!")
        else
          Ok(Json.obj("response" -> "funds added to wishlist"))));*/
    Ok

  }
  
/*
  var schemes: List[SchemeModel];
*/

  def removeFromWishlist(id: Int) = Action { request => 
    //schemes.filterNot { schemes => schemes.scheme_id == id }
    Ok
  }

  def getWishlistData = Action { request =>
    Ok
  }

}