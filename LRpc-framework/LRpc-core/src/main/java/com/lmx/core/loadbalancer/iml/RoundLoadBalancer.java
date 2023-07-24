package com.lmx.core.loadbalancer.iml;

import com.lmx.core.discovery.Registry;
import com.lmx.core.loadbalancer.AbstractLoadBalancer;
import com.lmx.core.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector() {
        return new RandomSelector();
    }


    /**
     * 执行核心算法
     */
    private static class RandomSelector implements Selector {

        private static AtomicInteger currentIndex = new AtomicInteger(0);

        @Override
        public InetSocketAddress getSelector(List<InetSocketAddress> serviceIntaddress) {
            if (serviceIntaddress == null || serviceIntaddress.isEmpty()) {
                throw new IllegalArgumentException("Service address list is empty or null.");
            }

            // Get the next address based on the currentIndex atomically
            int index = currentIndex.getAndIncrement();
            index = index < 0 ? 0 : index; // Handle integer overflow

            // Calculate the next index in a thread-safe manner
            int nextIndex = index % serviceIntaddress.size();

            // Get the selected address
            InetSocketAddress selectedAddress = serviceIntaddress.get(nextIndex);

            return selectedAddress;

        }


    }


}
