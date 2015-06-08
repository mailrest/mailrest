/*
 *      Copyright (C) 2015 Noorq, Inc.
 *      All rights reserved.
 */
package controllers

import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok("MailRest.com API Application")
  }

}