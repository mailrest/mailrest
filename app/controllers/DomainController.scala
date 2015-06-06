/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers

import play.api._
import play.api.mvc._

object DomainController extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}