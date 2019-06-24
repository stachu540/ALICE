package ai.alice.api

interface Spec<T> {
    fun isSatisfiedBy(element: T): Boolean
}

interface Action<T> {
    fun execute(t: T)
}

typealias UnknownDomainObjectException = AliceException
typealias AliceException = RuntimeException

interface Transformer<OUT, IN> {
    fun transform(`in`: IN): OUT
}