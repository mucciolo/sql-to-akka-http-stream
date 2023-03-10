package com.mucciolo.util

import org.slf4j.{Logger, LoggerFactory}

trait Log {
  protected lazy val log: Logger = LoggerFactory.getLogger(getClass)
}
