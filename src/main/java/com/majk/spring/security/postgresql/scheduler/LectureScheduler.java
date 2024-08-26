
package com.majk.spring.security.postgresql.scheduler;

/**
 *
 * @author Majkel
 */
import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.security.services.LectureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class LectureScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(LectureScheduler.class);
    private final LectureService lectureService;

    public LectureScheduler(LectureService lectureService) {
        this.lectureService = lectureService;    }

    @Scheduled(fixedRate = 60000) // Runs every minute
    @Transactional
    public void updateLectureStatus() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Lecture> activeLectures = lectureService.findActiveLectures();

        for (Lecture lecture : activeLectures) {

            if (currentDateTime.isAfter(lecture.getTimeLimit())) {

                logger.info("Setting lecture '{}' as inactive.", lecture.getName());
                lecture.setActive(false);

                lecture.setIpAddress(null);

                lectureService.saveLecture(lecture);
            }
}}}
    