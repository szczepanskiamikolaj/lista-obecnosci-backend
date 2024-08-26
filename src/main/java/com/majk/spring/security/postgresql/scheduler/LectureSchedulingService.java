
package com.majk.spring.security.postgresql.scheduler;

/**
 *
 * @author Majkel
 */
import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.security.services.LectureService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LectureSchedulingService {

    private final LectureService lectureService;

    public LectureSchedulingService(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    public void scheduleLectureDeactivation(LocalDateTime deactivationTime, Long lectureId) {
        long initialDelay = calculateInitialDelay(deactivationTime);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> {
            deactivateLecture(lectureId);
            scheduler.shutdown();
        }, initialDelay, TimeUnit.MILLISECONDS);
    }

    private long calculateInitialDelay(LocalDateTime deactivationTime) {
        LocalDateTime now = LocalDateTime.now();
        return TimeUnit.MILLISECONDS.toMillis(now.until(deactivationTime, ChronoUnit.MILLIS));
    }

    private void deactivateLecture(Long lectureId) {
        Lecture lecture = lectureService.getLectureById(lectureId);

        if (lecture != null) {
            lecture.setActive(false);
            lecture.setIpAddress(null);  
            lectureService.saveLecture(lecture);
        } else {
            throw new LectureNotFoundException("Lecture with ID " + lectureId + " not found.");
        }
    }
}

