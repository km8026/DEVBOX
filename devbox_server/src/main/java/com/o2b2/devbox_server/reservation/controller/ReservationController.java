package com.o2b2.devbox_server.reservation.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.o2b2.devbox_server.reservation.model.Reservation;
import com.o2b2.devbox_server.reservation.repository.ReservationRepository;
import com.o2b2.devbox_server.user.dto.CustomUserDetails;
import com.o2b2.devbox_server.user.entity.UserEntity;

@RestController
@CrossOrigin
public class ReservationController {
    @Autowired
    ReservationRepository reservationRepository;

    @PostMapping("/reservation/write")
    public Map<String, Object> reservation(
            @RequestBody Reservation reservation,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserEntity userEntity = userDetails.getUserEntity();
        reservation.setUserEntity(userEntity);
        Reservation result = reservationRepository.save(reservation);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "입력 완료");
        map.put("result", result);
        return map;
    }

    @GetMapping("/reservation/write/{date}")
    public List<Reservation> reservationList(@PathVariable("date") String date) {
        List<Reservation> list = reservationRepository.findByDate(date);
        return list;
    }

    @GetMapping("/reservation/list/{category}/{date}")
    public List<Map<String, Object>> reservationList(
            @PathVariable("category") String category,
            @PathVariable("date") String date,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String todayDate = today.format(dateFormatter);
        String todayTime = today.format(timeFormatter);

        List<Reservation> originList = reservationRepository.findAll();
        for (Reservation r : originList) {
            // r.getTime()에서 시작 시간 추출
            String dataTime = r.getTime().split(" - ")[0];
            // 날짜와 시간 비교 로직
            if (todayDate.compareTo(r.getDate()) > 0) { // todayDate > r.getDate()
                r.setCondition("사용완료"); // "사용완료"로 업데이트
                reservationRepository.save(r); // DB에 저장
            } else if (todayDate.compareTo(r.getDate()) == 0) { // todayDate == r.getDate()
                if (todayTime.compareTo(dataTime) >= 0) { // todayTime >= r.getTime()에서 추출한 시작 시간
                    r.setCondition("사용완료"); // "사용완료"로 업데이트
                    reservationRepository.save(r); // DB에 저장
                }
            }
        }

        Sort sort = Sort.by(Order.asc("date"), Order.asc("time"));
        Pageable pageable = PageRequest.of(page - 1, 4, sort);
        Page<Reservation> p = null;
        if (date.equals("All")) {
            p = reservationRepository.findByCondition(category, pageable);
        } else {
            p = reservationRepository.findByConditionAndDateContaining(category, date, pageable);
        }
        List<Reservation> list = p.getContent();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Reservation r : list) {
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("id", r.getId());
            rMap.put("date", r.getDate());
            rMap.put("time", r.getTime());
            rMap.put("condition", r.getCondition());
            rMap.put("userId", r.getUserEntity().getName());
            response.add(rMap);
        }
        int totalPage = p.getTotalPages();
        int startPage = (page - 1) / 10 * 10 + 1;
        int endPage = startPage + 9;
        if (endPage > totalPage) {
            endPage = totalPage;
        }
        Map<String, Object> pMap = new HashMap<>();
        pMap.put("totalPage", totalPage);
        pMap.put("startPage", startPage);
        pMap.put("endPage", endPage);
        pMap.put("currentPage", page);
        response.add(pMap);
        return response;
    }

    @GetMapping("/reservation/delete")
    public String reservationDelete(@RequestParam Long reservationId) {
        reservationRepository.deleteById(reservationId);
        return "삭제 완료";
    }

}
