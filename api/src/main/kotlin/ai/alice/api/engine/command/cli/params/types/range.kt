package ai.alice.api.engine.command.cli.params.types

import ai.alice.api.engine.command.cli.params.ArgumentProcessor
import ai.alice.api.engine.command.cli.params.ValuedOption

private inline fun <T> checkRange(
    it: T, min: T? = null, max: T? = null,
    clamp: Boolean, fail: (String) -> Unit
): T
        where T : Number, T : Comparable<T> {
    require(min == null || max == null || min < max) { "min must be less than max" }
    if (clamp) {
        if (min != null && it < min) return min
        if (max != null && it > max) return max
    } else if (min != null && it < min || max != null && it > max) {
        fail(
            when {
                min == null -> "$it is larger than the maximum valid value of $max."
                max == null -> "$it is smaller than the minimum valid value of $min."
                else -> "$it is not in the valid range of $min to $max."
            }
        )
    }
    return it
}


// Arguments
fun <T> ArgumentProcessor<T, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : ArgumentProcessor<T, T> where T : Number, T : Comparable<T> {
    return clone({ checkRange(transformValue(it), min, max, clamp) { fail(it) } }, transformAll, validator)
}

fun <T> ArgumentProcessor<T, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)

// Options
fun <T> ValuedOption<T?, T, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : ValuedOption<T?, T, T> where T : Number, T : Comparable<T> {
    return clone(
        { checkRange(transformValue(it), min, max, clamp) { fail(it) } },
        transformEach,
        transformAll,
        validator
    )
}

fun <T> ValuedOption<T?, T, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)