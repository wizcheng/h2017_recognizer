package org.hackathon2017.transcriber.api;

import org.hackathon2017.transcriber.ExternalConfig;
import org.hackathon2017.transcriber.workflow.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
public class ProcessApi {

    private ExternalConfig externalConfig;
    private ExecutorService executorService;
    private ConcurrentHashMap<String, Future<?>> map = new ConcurrentHashMap();

    @Autowired
    public ProcessApi(ExternalConfig externalConfig) {
        this.externalConfig = externalConfig;
        this.executorService = Executors.newCachedThreadPool();
    }


    @RequestMapping(value = "/process/cancel/{voiceId}", method = RequestMethod.POST)
    void cancelTask(@PathParam("voiceId") String voiceId) {

        Future<?> future = map.get(voiceId);
        if (future != null && !future.isCancelled() && !future.isCancelled()) {
            future.cancel(true);
        }
        new Workflow(externalConfig).cancel(voiceId);

    }

    @RequestMapping(value = "/process/submit", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseBody
    String submitProcess(@RequestParam MultipartFile file, HttpServletResponse response) throws Exception {


        byte[] bytes = file.getBytes();
        String filename = file.getOriginalFilename();
        String voiceId = UUID.randomUUID().toString();
        CountDownLatch latch = new CountDownLatch(1);
        Workflow workflow = new Workflow(externalConfig);
        workflow.updateProcess(voiceId, "Queue");
        workflow.updateMainProcess(voiceId, "queue", "");
        Future<?> future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    workflow.process(bytes, filename, voiceId);
                } finally {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    map.remove(voiceId);
                }
            }
        });
        map.put(voiceId, future);
        latch.countDown();

        return voiceId;

    }
}
