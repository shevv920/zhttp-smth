package backend.routes

import zhttp.*
import zhttp.http.*

import backend.routes.*

object Routes {
  private val health: Http[Any, Nothing, Request, Response] = Http.collect { case Method.GET -> !! / "health" =>
    Response.text("ok")
  }

  val public = Http.collectHttp[Request] {
    case _ -> !! / "health" =>
      health
    case _ -> "" /: "accounts" /: path =>
      Accounts.public.setPath(path)
  }

  val authed = Http.collectHttp[Request] { case _ -> "fruits" /: path =>
    Fruits.closed.setPath(path)
  }
}
