package org.example.search.owl;

public class OwlException extends RuntimeException {
    public OwlException() {
    }

    public OwlException(String s) {
        super(s);
    }

    public OwlException(Throwable throwable) {
        super(throwable);
    }
}
