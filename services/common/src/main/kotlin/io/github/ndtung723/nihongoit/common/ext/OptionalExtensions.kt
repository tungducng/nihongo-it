package io.github.ndtung723.nihongoit.common.ext

import io.github.ndtung723.nihongoit.common.exception.BusinessException
import java.util.Optional

fun <T> Optional<T>.orThrow(message: String): T = orElseThrow { BusinessException(message) }
