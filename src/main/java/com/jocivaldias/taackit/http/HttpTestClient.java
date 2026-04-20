package com.jocivaldias.taackit.http;

import io.restassured.response.Response;

public interface HttpTestClient {

    Response execute(HttpRequestSpec spec);
}
