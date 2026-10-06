package com.group06.restaurantevent.config;
import com.group06.restaurantevent.staff.entity.StaffProfile;
import com.group06.restaurantevent.staff.repository.StaffProfileRepository;
import com.group06.restaurantevent.users.repository.UserRepository;
import com.group06.restaurantevent.common.enums.EmploymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
/** Backfills missing profiles so existing staff accounts can be edited and assigned. */
@Component @Order(100) @RequiredArgsConstructor
public class StaffProfileSeeder implements CommandLineRunner {
 private final UserRepository users;
 private final StaffProfileRepository profiles;
 @Override @Transactional public void run(String... args) {
  for(var user : users.findAllExcludingRole("CUSTOMER")) {
   if(profiles.findByUserId(user.getId()).isPresent()) continue;
   profiles.save(StaffProfile.builder().userId(user.getId()).employeeCode("EMP-U"+user.getId())
    .jobTitle("Team member").employmentStatus(EmploymentStatus.FULL_TIME).joinedDate(user.getCreatedAt().toLocalDate()).isActive(user.isActive()).build());
  }
 }
}
