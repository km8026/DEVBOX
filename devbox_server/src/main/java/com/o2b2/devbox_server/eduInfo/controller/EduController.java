package com.o2b2.devbox_server.eduInfo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.o2b2.devbox_server.eduInfo.model.EduEntity;
import com.o2b2.devbox_server.eduInfo.repository.EduRepository;

import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@CrossOrigin
@Transactional
public class EduController {

    private final Path fileStorageLocation = Paths.get("c:/images"); // 파일 저장 경로

    @Autowired
    EduRepository eduRepository;



    @GetMapping("/edu/list/{state}")
    public Map<String, Object> eduList(
            @PathVariable("state") String state,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "search", required = false) String search) {

        System.out.println(state);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page - 1, size, sort); // 페이지 요청 생성

        Page<EduEntity> p = eduRepository.findByState(state, pageable);
        List<EduEntity> list = p.getContent();

        // 페이지네이션 관련 정보 계산
        int totalPage = p.getTotalPages();
        int startPage = (page - 1) / 10 * 10 + 1;
        int endPage = Math.min(startPage + 9, totalPage);

        // 반환할 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("list", list.stream().map(edu -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", edu.getId());
            map.put("title", edu.getTitle());
            map.put("subtitle", edu.getSubtitle());
            map.put("img", edu.getImg());
            map.put("recruit", edu.getRecruit());
            map.put("eduterm", edu.getEduterm());
            map.put("people", edu.getPeople());
            map.put("link", edu.getLink());
            map.put("logo", edu.getLogo());
            return map;
        }).collect(Collectors.toList())); // EduEntity를 Map으로 변환
        response.put("currentPage", page); // 현재 페이지
        response.put("startPage", startPage); // 시작 페이지
        response.put("endPage", endPage); // 끝 페이지
        response.put("totalPage", totalPage); // 전체 페이지 수

        return response; // JSON 형태로 반환
    }


    @PostMapping("/edu")
    public Map<String, Object> edu(
            @ModelAttribute EduEntity edu,
            @RequestParam("file") MultipartFile file) {
        System.out.println(edu);
        System.out.println(file.getOriginalFilename());
         
        Map<String, Object> map = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            map.put("code", 400);
            map.put("msg", "포스터를 첨부해주세요.");
            return map;
        }


        edu.setImg(file.getOriginalFilename());
        EduEntity result = eduRepository.save(edu);

        try {
            file.transferTo(new File("c:/images/" + file.getOriginalFilename()));
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            map.put("code", 500);
            map.put("msg", "업로드 중 오류 발생: " + e.getMessage());
            return map;
        }

        map.put("code", 200);
        map.put("msg", "업로드완료");

        return map;
    }

    @PostMapping("/update")
    public Map<String, Object> update(
            @ModelAttribute EduEntity edu,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        System.out.println(edu);

        Map<String, Object> map = new HashMap<>();

        // 데이터베이스에서 기존의 EduEntity를 가져옵니다.
        EduEntity existingEdu = eduRepository.findById(edu.getId()).orElse(null);

        if (existingEdu != null) {
            // 파일이 새로 업로드되지 않은 경우 기존의 파일 이름을 유지합니다.
            if (file != null && !file.isEmpty()) {
                // 파일이 새로 업로드된 경우 새로운 파일 이름을 설정합니다.
                edu.setImg(file.getOriginalFilename());

                try {
                    // 새 파일을 지정된 경로에 저장합니다.
                    file.transferTo(new File("c:/images/" + file.getOriginalFilename()));
                } catch (IllegalStateException | IOException e) {
                    e.printStackTrace(); // 파일 저장 중 오류가 발생하면 스택 트레이스를 출력합니다.
                }
            } else {
                // 파일이 없으면 기존의 파일 이름을 유지합니다.
                edu.setImg(existingEdu.getImg());
            }

            // EduEntity 객체를 데이터베이스에 저장합니다.
            eduRepository.save(edu);
        } else {
            // 기존 EduEntity가 없는 경우의 처리 (일반적으로 여기까지 도달하지 않아야 합니다).
            map.put("code", 404);
            map.put("msg", "존재하지 않는 데이터입니다.");
            return map;
        }

        // 응답 맵에 성공 코드와 메시지를 추가합니다.
        map.put("code", 200);
        map.put("msg", "수정완료");

        return map; // 수정 완료 응답을 반환합니다.
    }

    @DeleteMapping("/edu/delete")
    public String edudelete(@RequestParam Long Id) {
        eduRepository.deleteById(Id);
        return "삭제 완료";
    }

    @GetMapping("/edu/detail")
    @ResponseBody
    public Map<String, Object> eduDetail(@RequestParam Long id) {
        Map<String, Object> map = new HashMap<>();

        // id로 데이터베이스에서 교육 정보를 조회합니다.
        Optional<EduEntity> eduOpt = eduRepository.findById(id);

        EduEntity edu = eduOpt.get();
        map.put("id", edu.getId());
        map.put("title", edu.getTitle());
        map.put("subtitle", edu.getSubtitle());
        map.put("img", edu.getImg());
        map.put("recruit", edu.getRecruit());
        map.put("eduterm", edu.getEduterm());
        map.put("people", edu.getPeople());
        map.put("link", edu.getLink());
        map.put("logo", edu.getLogo());
        map.put("state", edu.getState());

        return map;

    }

    @GetMapping("/edu/update")
    @ResponseBody
    public Map<String, Object> eduUpdate(@RequestParam Long id) {
        Map<String, Object> map = new HashMap<>();

        // id로 데이터베이스에서 교육 정보를 조회합니다.
        Optional<EduEntity> eduOpt = eduRepository.findById(id);

        EduEntity edu = eduOpt.get();
        map.put("id", edu.getId());
        map.put("title", edu.getTitle());
        map.put("subtitle", edu.getSubtitle());
        map.put("img", edu.getImg());
        map.put("recruit", edu.getRecruit());
        map.put("eduterm", edu.getEduterm());
        map.put("people", edu.getPeople());
        map.put("link", edu.getLink());
        map.put("logo", edu.getLogo());
        map.put("state", edu.getState());

        return map;

    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam Long id) {
        try {
            Optional<EduEntity> opt = eduRepository.findById(id);
            String filename = opt.get().getImg();
            // 파일 경로 생성
            Path filePath = fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 파일이 존재하지 않을 경우 예외 처리
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 파일의 MIME 타입을 추측 (추가 설정 가능)
            String contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException ex) {
            // 파일 읽기 오류 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
