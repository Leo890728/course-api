package fcu.pbiecs.spring_demo.controller;

import fcu.pbiecs.spring_demo.dto.PopularCourseDTO;
import fcu.pbiecs.spring_demo.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "統計分析", description = "提供課程統計分析 API")
@RestController
@RequestMapping("api/statistics")
public class StatisticsController {

    @Autowired
    EnrollmentService enrollmentService;

    @Operation(summary = "查詢熱門課程", description = "取得最熱門課程前N名（按選課人數排序）")
    @GetMapping("/popular-courses")
    public List<PopularCourseDTO> getPopularCourses(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        if (limit <= 0 || limit > 50) {
            throw new IllegalArgumentException("Limit must be between 1 and 50");
        }
        return enrollmentService.getTopPopularCourses(limit);
    }
}