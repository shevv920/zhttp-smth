package common

object Common {
  final case class SomeCommonData(value: String, value2: Int)

  val commonValue: SomeCommonData = SomeCommonData("hi", 1337)
}
