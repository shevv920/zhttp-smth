package backend.repositories

import common.Protocol.{ CreateFruitRequest, Fruit, RequestParseError }
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.jdbczio.Quill
import zhttp.http.{ uuid as uuidPath, * }
import zio.{ ULayer, ZIO, ZLayer }

import java.sql.SQLException
import java.util.UUID
import io.getquill.{ EntityQuery, Quoted }

import javax.sql.DataSource

trait FruitRepo extends Resource {
  def get(page: Page, limit: Limit): ZIO[Any, SQLException, List[Fruit]]
  def getById(id: UUID): ZIO[Any, SQLException, Option[Fruit]]
  def insert(createFruitRequest: CreateFruitRequest): ZIO[Any, SQLException, Long]
}

object FruitRepo extends Resource {
  import Resource.*
  import zio.json.*

  def get(
      page: Page = defaultPage,
      limit: Limit = defaultLimit,
  ): ZIO[FruitRepo, SQLException, List[Fruit]] = ZIO.serviceWithZIO[FruitRepo](_.get(page, limit))

  def getById(id: UUID): ZIO[FruitRepo, SQLException, Option[Fruit]] = ZIO.serviceWithZIO[FruitRepo](_.getById(id))

  def insert(fruit: CreateFruitRequest): ZIO[FruitRepo, SQLException, Long] =
    ZIO.serviceWithZIO[FruitRepo](_.insert(fruit))

}

final case class FruitRepoLive(quill: Quill.Postgres[io.getquill.SnakeCase]) extends FruitRepo {
  import io.getquill._
  import quill.*

  inline def fruits =
    quote {
      querySchema[Fruit]("fruits")
    }

  override def get(page: Page, limit: Limit): ZIO[Any, SQLException, List[Fruit]] =
    run(quote(fruits.drop(lift((page - 1) * limit)).take(lift(limit.toInt))))

  override def getById(id: UUID): ZIO[Any, SQLException, Option[Fruit]] =
    run(quote(fruits.filter(fr => fr.id == lift(id)))).map(_.headOption)

  override def insert(createFruitRequest: CreateFruitRequest): ZIO[Any, SQLException, Long] =
    run(quote(fruits.insert(_.name -> lift(createFruitRequest.name))))
}

object FruitRepoLive:
  val live = ZLayer.fromFunction(FruitRepoLive.apply _)
