package com.github.dapeng.core;

import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 代表一个服务的运行实例
 *
 * @author lihuimin
 * @date 2017/12/25
 */
public class RuntimeInstance {

    public final String service;
    public final String version;
    public final String ip;
    public final int port;
    public final String temp_seqid; //临时节点序号
    public int weight;

    /**
     * 该服务实例在某客户端的调用计数
     */
    private AtomicInteger activeCount = new AtomicInteger(0);

    public RuntimeInstance(String service, String ip, int port, String version, String temp_seqid,String weightData) {
        this.service = service;
        this.version = version;
        this.ip = ip;
        this.port = port;
        this.temp_seqid = temp_seqid;
        this.weight = weightData == null?SoaSystemEnvProperties.SOA_INSTANCE_WEIGHT:Integer.parseInt(weightData);
    }

    public AtomicInteger getActiveCount() {
        return activeCount;
    }

    /**
     * 调用计数+1
     *
     * @return 操作后的计数值
     */
    public int increaseActiveCount() {
        return activeCount.incrementAndGet();
    }

    /**
     * 调用计数-1
     *
     * @return 操作后的计数值
     */
    public int decreaseActiveCount() {
        return activeCount.decrementAndGet();
    }


    public String getTemp_seqid() {
        return temp_seqid;
    }

    /**
     * ip:port:version
     *
     * @return
     */
    public String getInstanceInfo() {
        return ip + ":" + port + ":" + version;
    }


    /**
     * ip:port:version:seq_id
     *
     * @return
     */
    public String getEqualStr() {
        return ip + ":" + port + ":" + version + ":" + temp_seqid;
    }

    @Override
    public String toString() {
        return "IP:【" + ip + ":" + port + '】';
    }
}
