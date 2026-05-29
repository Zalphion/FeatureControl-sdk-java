package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.lib.http.HttpUrlConnectionHttpFunction;

public class HttpApplicationSourceTest extends ApplicationSourceContract {

    public HttpApplicationSourceTest() {
        super(new HttpUrlConnectionHttpFunction());
    }
}
