package controllers

import java.sql.Connection

import javax.inject.Inject
import play.api.mvc.{Action, AbstractController, ControllerComponents}

class Application  @Inject()
(cc: ControllerComponents)
  extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.welcome("test message"))
  }

  def display(id : Int) = Action { request =>
    Ok(views.html.display("display banner", diagramID = id))
  }
}
