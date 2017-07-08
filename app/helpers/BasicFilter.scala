package helpers

import javax.inject.Inject

import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

class BasicFilter @Inject() (corsFilter: CORSFilter)
  extends DefaultHttpFilters(corsFilter) {

}
