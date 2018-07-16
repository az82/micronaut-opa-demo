package de.az.demo.mn.opa;

/**
 * OPA Data API response
 *
 * @param <T> result type
 */
class OpaDataResponse<T> {

    private T result;

    public T getResult() {
        return this.result;
    }

    public void setResult(T result) {
        this.result = result;
    }

}
