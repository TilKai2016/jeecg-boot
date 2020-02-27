package com.aill.sso.oauth.client.pojo.bo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class OkHttpResponse {

	private int status;
	private String response;
}
