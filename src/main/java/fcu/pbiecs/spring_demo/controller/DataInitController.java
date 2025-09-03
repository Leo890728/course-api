package fcu.pbiecs.spring_demo.controller;

import fcu.pbiecs.spring_demo.service.DataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DataInitController {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @GetMapping(value = "/init", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter initializeDataWithProgress(
            @RequestParam(defaultValue = "10000") int studentCount,
            @RequestParam(defaultValue = "100") int teacherCount, 
            @RequestParam(defaultValue = "50") int courseCount,
            @RequestParam(defaultValue = "1000000") int enrollmentCount) {
        
        SseEmitter emitter = new SseEmitter(1800000L); // 30 minutes timeout for large data
        
        CompletableFuture.runAsync(() -> {
            try {
                // 驗證參數
                if (studentCount <= 0 || teacherCount <= 0 || courseCount <= 0 || enrollmentCount <= 0) {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("success", false, "message", "所有參數必須大於0")));
                    emitter.complete();
                    return;
                }
                
                // 動態檢查CSV檔案中的課程數量
                // 這個檢查移到DataGeneratorService中進行
                
                if (enrollmentCount > studentCount * courseCount) {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("success", false, "message", "選課數量不能超過學生數量 × 課程數量")));
                    emitter.complete();
                    return;
                }

                long startTime = System.currentTimeMillis();
                
                // 發送開始事件
                emitter.send(SseEmitter.event()
                    .name("start")
                    .data(Map.of("message", "開始生成資料...")));

                // 執行資料生成並推送進度
                dataGeneratorService.generateRandomData(studentCount, teacherCount, courseCount, enrollmentCount, 
                    (step, current, total) -> {
                        try {
                            int percentage = (int) ((current * 100.0) / total);
                            emitter.send(SseEmitter.event()
                                .name("progress")
                                .data(Map.of(
                                    "step", step,
                                    "current", current,
                                    "total", total,
                                    "percentage", percentage
                                )));
                            
                            // 發送心跳保持連接
                            emitter.send(SseEmitter.event()
                                .name("heartbeat")
                                .data("alive"));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    });
                
                long endTime = System.currentTimeMillis();
                
                // 發送完成事件
                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of(
                        "success", true,
                        "message", "資料初始化完成",
                        "data", Map.of(
                            "studentCount", studentCount,
                            "teacherCount", teacherCount,
                            "courseCount", courseCount,
                            "enrollmentCount", enrollmentCount,
                            "executionTime", (endTime - startTime) + "ms"
                        )
                    )));
                
                emitter.complete();
                
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("success", false, "message", "資料初始化失敗: " + e.getMessage())));
                } catch (IOException ioException) {
                    emitter.completeWithError(ioException);
                }
                emitter.complete();
            }
        });
        
        return emitter;
    }
    
    // 保留原有的同步API作為備選
    @PostMapping("/init-sync")
    public ResponseEntity<Map<String, Object>> initializeDataSync(
            @RequestParam(defaultValue = "10000") int studentCount,
            @RequestParam(defaultValue = "100") int teacherCount, 
            @RequestParam(defaultValue = "50") int courseCount,
            @RequestParam(defaultValue = "1000000") int enrollmentCount) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 驗證參數
            if (studentCount <= 0 || teacherCount <= 0 || courseCount <= 0 || enrollmentCount <= 0) {
                response.put("success", false);
                response.put("message", "所有參數必須大於0");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 動態檢查CSV檔案中的課程數量，移到DataGeneratorService中進行
            
            if (enrollmentCount > studentCount * courseCount) {
                response.put("success", false);
                response.put("message", "選課數量不能超過學生數量 × 課程數量");
                return ResponseEntity.badRequest().body(response);
            }

            long startTime = System.currentTimeMillis();
            
            // 執行資料生成
            dataGeneratorService.generateRandomData(studentCount, teacherCount, courseCount, enrollmentCount);
            
            long endTime = System.currentTimeMillis();
            
            response.put("success", true);
            response.put("message", "資料初始化完成");
            response.put("data", Map.of(
                "studentCount", studentCount,
                "teacherCount", teacherCount,
                "courseCount", courseCount,
                "enrollmentCount", enrollmentCount,
                "executionTime", (endTime - startTime) + "ms"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "資料初始化失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDataInfo() {
        Map<String, Object> response = new HashMap<>();
        
        // 獲取可用課程數量
        int availableCourses = dataGeneratorService.getAvailableCourseCount();
        
        response.put("description", "隨機資料生成API");
        response.put("endpoint", "/api/data/init");
        response.put("method", "GET");
        response.put("parameters", Map.of(
            "studentCount", "學生數量 (預設: 50)",
            "teacherCount", "老師數量 (預設: 10)",
            "courseCount", String.format("課程數量 (預設: 20, 最大: %d)", availableCourses),
            "enrollmentCount", "選課數量 (預設: 100)"
        ));
        response.put("example", String.format("GET /api/data/init?studentCount=100&teacherCount=15&courseCount=%d&enrollmentCount=200", Math.min(50, availableCourses)));
        response.put("note", "此API會清除現有資料並重新生成隨機資料");
        response.put("availableCourses", availableCourses);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/test-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testSse() {
        SseEmitter emitter = new SseEmitter(30000L); // 30 seconds timeout
        
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000); // 等待1秒
                    emitter.send(SseEmitter.event()
                        .name("test")
                        .data(Map.of("count", i, "message", "測試訊息 " + i)));
                }
                
                emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of("message", "測試完成")));
                
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用專門的清除方法
            dataGeneratorService.clearAllData();
            
            response.put("success", true);
            response.put("message", "所有資料已清除");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清除資料失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}