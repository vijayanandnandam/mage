package helpers

import javax.inject.Inject

import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

/**
  * Created by fincash on 10-03-2017.
  */
class ProductionHttpFilter @Inject()(corsFilter: CORSFilter, gzipFilter: GzipFilter, securityHeadersFilter: SecurityHeadersFilter)
  extends DefaultHttpFilters(corsFilter,gzipFilter,securityHeadersFilter){

}
