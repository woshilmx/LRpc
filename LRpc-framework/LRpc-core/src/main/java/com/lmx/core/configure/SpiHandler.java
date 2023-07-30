package com.lmx.core.configure;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiHandler {

    private static final String basePath = "META-INF/services";

    public static Map<String, List<String>> SPI_CANCH = new ConcurrentHashMap<>();


    public static Map<Class, List<ObjectWapper>> SPI_WAPPER_CANCH = new ConcurrentHashMap<>();

    static {
        URL resource = Spiresolver.class.getClassLoader().getResource(basePath);
        if (resource != null) {
            File file = new File(resource.getPath());
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    try {
                        ArrayList<String> strings = new ArrayList<>();
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                        String line = null;
                        if ((line = bufferedReader.readLine()) != null) {
                            strings.add(line);
                        }
                        SPI_CANCH.put(f.getName(), strings);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 获取包装类
     * */
    public static <T> ObjectWapper<T> get(Class<T> tClass) {

        List<ObjectWapper> objectWappers = SPI_WAPPER_CANCH.get(tClass);

        if (objectWappers != null) {
            return (ObjectWapper<T>) objectWappers.get(0);
        }
        String name = tClass.getName();
        List<String> list = SPI_CANCH.get(name);
        if (list == null) {
            log.info("spi中没有此类{}", name);
            return null;
        }

        List<ObjectWapper> ts = new ArrayList<>();
//        获取其中的类名
        for (String line : list) {
            String[] split = line.split("-");
            ObjectWapper<T> tObjectWapper = new ObjectWapper<>();
            tObjectWapper.setCode(Byte.parseByte(split[0]));
            tObjectWapper.setName(split[1]);
            try {
                Class<?> aClass = Class.forName(split[2]);
                T o = (T) aClass.getConstructor().newInstance();
                tObjectWapper.setData(o);
                ts.add(tObjectWapper);
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
        SPI_WAPPER_CANCH.put(tClass, ts);
        return ts.get(0);
    }
}
