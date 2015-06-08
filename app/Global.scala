import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.Application
import play.api.GlobalSettings
import play.api.mvc.WithFilters
import filters.BasicAuthFilter

object Global extends WithFilters(BasicAuthFilter) with GlobalSettings with LazyLogging {

    override def onStart(app: Application) {
      logger.info("Application has started")
    }  
  
    override def onStop(app: Application) {
      logger.info("Application shutdown...")
    }  
    
  
}