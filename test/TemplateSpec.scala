import collection.mutable.Stack
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.fusesource.scalate.TemplateSource
import org.fusesource.scalate.TemplateEngine
import java.util.UUID


@RunWith(classOf[JUnitRunner])
class TemplateSpec extends FlatSpec with Matchers {
  
  "template" should "be generated" in {
    
    var params = Map(
        "name" -> "Alex"
    );
    
    var source = TemplateSource.fromText("templateName.mustache", "Hello {{name}}");
    
    val engine = new TemplateEngine

    
    var out = engine.layout(source, params)
    
    out should be ("Hello Alex")
  }
  
}