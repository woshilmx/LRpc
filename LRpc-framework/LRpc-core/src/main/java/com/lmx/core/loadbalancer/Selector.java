package com.lmx.core.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface Selector {

    InetSocketAddress getSelector(List<InetSocketAddress> serviceIntaddress);
}
