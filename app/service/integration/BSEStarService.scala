package service.integration

import service.PropertiesLoaderService

abstract class BSEStarService {
  
  def getPropertiesConfig() = {

    PropertiesLoaderService.getConfig
  }
  
}