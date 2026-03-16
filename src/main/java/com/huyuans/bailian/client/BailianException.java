package com.huyuans.bailian.client;

import lombok.Getter;


















@Getter
public class BailianException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    
    private final int httpStatus;

    
    private final String responseBody;

    




    public BailianException(String message) {
        super(message);
        this.httpStatus = 0;
        this.responseBody = null;
    }

    






    public BailianException(String message, int httpStatus, String responseBody) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    





    public BailianException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 0;
        this.responseBody = null;
    }
}