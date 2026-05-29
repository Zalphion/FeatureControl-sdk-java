package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.lib.http.OkHttp3HttpFunction;

import java.io.IOException;

public class OkHttp3ApplicationSourceTest extends ApplicationSourceContract {

    public OkHttp3ApplicationSourceTest() throws IOException {
        super(new OkHttp3HttpFunction());
    }
}
