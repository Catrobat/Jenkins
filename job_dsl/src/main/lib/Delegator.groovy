/**
 * Delegates all unknown method calls to the provided object.
 *
 * Both method calls can be delegated as well a closures.
 * Closures look first for methods in subclasses of Delegator
 * to then look in the given delegate.
 */
class Delegator {
    def delegate

    Delegator(def delegate) {
        this.delegate = delegate
    }

    def methodMissing(String name, argument) {
        delegate.invokeMethod(name, (Object[]) argument)
    }

    void runClosure(Closure closure) {
        closure = closure.clone()
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
    }
}
