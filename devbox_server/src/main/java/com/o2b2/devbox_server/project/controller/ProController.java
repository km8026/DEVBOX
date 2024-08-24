package com.o2b2.devbox_server.project.controller;

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

import com.o2b2.devbox_server.project.model.ProEntity;
import com.o2b2.devbox_server.project.repository.ProRepository;

import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@CrossOrigin
@Transactional
public class ProController {

    private final Path fileStorageLocation = Paths.get("c:/images"); // 파일 저장 경로

    @Autowired
    ProRepository proRepository;



    @GetMapping("/pro/list/{state}")
    public Map<String, Object> proList(
            @PathVariable("state") String state,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "search", required = false) String search) {

        System.out.println(state);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page - 1, size, sort); // 페이지 요청 생성

        Page<ProEntity> p = proRepository.findByState(state, pageable);
        List<ProEntity> list = p.getContent();

        // 페이지네이션 관련 정보 계산
        int totalPage = p.getTotalPages();
        int startPage = (page - 1) / 10 * 10 + 1;
        int endPage = Math.min(startPage + 9, totalPage);

        // 반환할 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("list", list.stream().map(pro -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pro.getId());
            map.put("title", pro.getTitle());
            map.put("img", pro.getImg());
            map.put("link", pro.getLink());
            map.put("coment", pro.getComent());
            map.put("name", pro.getName());
            return map;
        }).collect(Collectors.toList())); // proEntity를 Map으로 변환
        response.put("currentPage", page); // 현재 페이지
        response.put("startPage", startPage); // 시작 페이지
        response.put("endPage", endPage); // 끝 페이지
        response.put("totalPage", totalPage); // 전체 페이지 수

        return response; // JSON 형태로 반환
    }


    @PostMapping("/pro")
    public Map<String, Object> pro(
            @ModelAttribute ProEntity pro,
            @RequestParam("file") MultipartFile file) {
        System.out.println(pro);
        System.out.println(file.getOriginalFilename());

        Map<String, Object> map = new HashMap<>();

        pro.setImg(file.getOriginalFilename());
        ProEntity result = proRepository.save(pro);

        try {
            file.transferTo(new File("c:/images/" + file.getOriginalFilename()));
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        map.put("code", 200);
        map.put("msg", "수정완료");

        return map;
    }

    @PostMapping("/pro/update")
    public Map<String, Object> update(
            @ModelAttribute ProEntity pro,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        System.out.println(pro);

        Map<String, Object> map = new HashMap<>();

        // 데이터베이스에서 기존의 proEntity를 가져옵니다.
        ProEntity existingpro = proRepository.findById(pro.getId()).orElse(null);

        if (existingpro != null) {
            // 파일이 새로 업로드되지 않은 경우 기존의 파일 이름을 유지합니다.
            if (file != null && !file.isEmpty()) {
                // 파일이 새로 업로드된 경우 새로운 파일 이름을 설정합니다.
                pro.setImg(file.getOriginalFilename());

                try {
                    // 새 파일을 지정된 경로에 저장합니다.
                    file.transferTo(new File("c:/images/" + file.getOriginalFilename()));
                } catch (IllegalStateException | IOException e) {
                    e.printStackTrace(); // 파일 저장 중 오류가 발생하면 스택 트레이스를 출력합니다.
                }
            } else {
                // 파일이 없으면 기존의 파일 이름을 유지합니다.
                pro.setImg(existingpro.getImg());
            }

            // proEntity 객체를 데이터베이스에 저장합니다.
            proRepository.save(pro);
        } else {
            // 기존 proEntity가 없는 경우의 처리 (일반적으로 여기까지 도달하지 않아야 합니다).
            map.put("code", 404);
            map.put("msg", "존재하지 않는 데이터입니다.");
            return map;
        }

        // 응답 맵에 성공 코드와 메시지를 추가합니다.
        map.put("code", 200);
        map.put("msg", "수정완료");

        return map; // 수정 완료 응답을 반환합니다.
    }

    @DeleteMapping("/pro/delete")
    public String prodelete(@RequestParam Long Id) {
        proRepository.deleteById(Id);
        return "삭제 완료";
    }

    @GetMapping("/pro/detail")
    @ResponseBody
    public Map<String, Object> proDetail(@RequestParam Long id) {
        Map<String, Object> map = new HashMap<>();

        // id로 데이터베이스에서 교육 정보를 조회합니다.
        Optional<ProEntity> proOpt = proRepository.findById(id);

        ProEntity pro = proOpt.get();
        map.put("id", pro.getId());
        map.put("title", pro.getTitle());
        map.put("img", pro.getImg());
        map.put("link", pro.getLink());
        map.put("coment", pro.getComent());
        map.put("name", pro.getName());

        return map;

    }

    @GetMapping("/pro/update")
    @ResponseBody
    public Map<String, Object> proUpdate(@RequestParam Long id) {
        Map<String, Object> map = new HashMap<>();

        // id로 데이터베이스에서 교육 정보를 조회합니다.
        Optional<ProEntity> proOpt = proRepository.findById(id);

        ProEntity pro = proOpt.get();
        map.put("id", pro.getId());
        map.put("title", pro.getTitle());
        map.put("img", pro.getImg());
        map.put("link", pro.getLink());
        map.put("coment", pro.getComent());
        map.put("name", pro.getName());

        return map;

    }

    @GetMapping("/pro/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam Long id) {
        try {
            Optional<ProEntity> opt = proRepository.findById(id);
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
