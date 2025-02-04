package eu.ubitech.video.app.flink.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.ubitech.video.app.flink.processor.VideoFaceDetection;
import eu.ubitech.video.app.flink.processor.VideoMotionDetector;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;

public class TransformFunction extends RichMapFunction<VideoEventStringData, String> {



    @Override
    public String map(VideoEventStringData in) throws Exception {
        HashMap<String, String> envHashMap = new HashMap<String, String>(System.getenv());

        final String processedImageDir = envHashMap.get(EnvKeys.PROCC_OUT_DIR.toString()).toString();
        final String frameImageDir = envHashMap.get(EnvKeys.FRAM_OUT_DIR.toString()).toString();

        //Detect-Face function
        VideoEventStringProcessed processedData = VideoFaceDetection.detectFace(in,processedImageDir,frameImageDir);

        Gson gson = new Gson();
        JsonObject obj = new JsonObject();

        //JSON Object that will be sent as message
        obj.addProperty("cameraId",processedData.getCameraId());
        obj.addProperty("timestamp", processedData.getTimestamp());
        obj.addProperty("rows", processedData.getRows());
        obj.addProperty("cols", processedData.getCols());
        obj.addProperty("type", processedData.getType());
        obj.addProperty("data",(processedData.getData()));
        obj.add("lista", new Gson().toJsonTree(processedData.getData())); //.getLista

        String json = gson.toJson(obj);

        return json;
    }
}