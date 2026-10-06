package com.group06.restaurantevent.staff.entity;

import com.group06.restaurantevent.common.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shift_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"shift_id", "staff_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "assigned_role", length = 50)
    private String assignedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }
}
