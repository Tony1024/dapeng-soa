package com.github.dapeng.impl.listener;

import com.github.dapeng.basic.api.counter.CounterServiceClient;
import com.github.dapeng.basic.api.counter.domain.DataPoint;
import com.github.dapeng.basic.api.counter.service.CounterService;
import com.github.dapeng.core.SoaException;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author huyj
 * @Created 2018-11-22 11:41
 */
public class TaskMonitorDataReportUtils {
    private static Logger logger = LoggerFactory.getLogger("container.scheduled.task");

    private static final int MAX_SIZE = 32;
    private static final int BATCH_MAX_SIZE = 50;

    public final static String TASK_DATABASE = "dapengTask";
    public final static String TASK_DATABASE_TABLE = "dapeng_task_info";
    private static CounterService COUNTER_CLIENT = new CounterServiceClient();
    private static final List<DataPoint> dataPointList = new ArrayList<>();
    private static final ArrayBlockingQueue<List<DataPoint>> taskDataQueue = new ArrayBlockingQueue<>(MAX_SIZE);

    //上送线程池
    private static final ExecutorService taskMonitorDataUploaderExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("dapeng-taskMonitorDataUploader-%d")
            .build());

    public static void appendDataPoint(List<DataPoint> uploadList) {
        synchronized (dataPointList) {
            dataPointList.addAll(uploadList);

            if (dataPointList.size() >= BATCH_MAX_SIZE) {
                try {
                    taskDataQueue.put(Lists.newArrayList(dataPointList));
                } catch (InterruptedException e) {
                    logger.error("TaskMonitorDataReportUtils::appendDataPoint taskDataQueue put is Interrupted", e);
                    logger.error(e.getMessage(), e);
                }
                dataPointList.clear();
            }
        }
    }

    public static void taskMonitorUploader() {
        // uploader point thread.
        taskMonitorDataUploaderExecutor.execute(() -> {
            while (true) {
                List<DataPoint> uploaderDataPointList = null;
                try {
                    uploaderDataPointList = taskDataQueue.take();
                    COUNTER_CLIENT.submitPoints(uploaderDataPointList);
                } catch (SoaException e) {
                    logger.error(e.getMsg(), e);
                    dataPointList.addAll(uploaderDataPointList);
                } catch (InterruptedException e) {
                    logger.error("TaskMonitorDataReportUtils::taskMonitorUploader taskDataQueue take is Interrupted", e);
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }
}
