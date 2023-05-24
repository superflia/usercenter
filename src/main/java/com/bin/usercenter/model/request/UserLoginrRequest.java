package com.bin.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginrRequest implements Serializable {

    private static final long serialVersionUID = -962600440650724783L;

    private String userAccount;
    private String userPassword;
}
