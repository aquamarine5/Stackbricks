package org.aquamarine5.brainspark.stackbricks

class ExceptionalResult<T>(
    val isSuccess: Boolean,
    val result: T?,
    val exception: Exception?,
    val helpText: String?
) {
    companion object {
        fun <T> success(result: T): ExceptionalResult<T> {
            return ExceptionalResult(true, result, null, null)
        }

        fun <T> fail(exception: Exception, helpText: String?): ExceptionalResult<T> {
            return ExceptionalResult(false, null, exception, helpText)
        }

        fun <T, K> fail(exceptionalResult: ExceptionalResult<K>): ExceptionalResult<T> {
            return ExceptionalResult(
                false,
                null,
                exceptionalResult.exception,
                exceptionalResult.helpText
            )
        }
    }
}