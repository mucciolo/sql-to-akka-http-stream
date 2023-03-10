package com.mucciolo

import io.circe.generic.JsonCodec

package object domain {
  @JsonCodec
  case class Data(id: Option[Long], value: Int)
}
