package me.serebryakov.animal_shelter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "reports")
@IdClass(ReportId.class)
public class Report {

    @Id
    @Column(name = "chat_id")
    private long chatId;

    @Id
    @Column(name = "date")
    private LocalDate date;

    @Id
    @Column(name = "shelter_id")
    private int shelterId;

    @Column(name = "report_time")
    private LocalTime time;

    @Column(name = "text")
    private String text;

    @Column(name = "file_id")
    private String fileId;
}