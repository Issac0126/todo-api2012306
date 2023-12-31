package com.example.todo.userapi.api;

import com.example.todo.auth.TokenUserInfo;
import com.example.todo.exception.DuplicatedEmailException;
import com.example.todo.exception.NoRegisteredArgumentsException;
import com.example.todo.userapi.dto.request.LoginRequestDTO;
import com.example.todo.userapi.dto.request.UserRequestSignUpDTO;
import com.example.todo.userapi.dto.request.UserSignUpResponseDTO;
import com.example.todo.userapi.dto.response.LoginResponseDTO;
import com.example.todo.userapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    
    private final UserService userService;
    
    //이메일 중복 확인 요청 처리
    // GET: /api/auth/check?email=zzzz@xxx.com
    @GetMapping("/check")
    public ResponseEntity<?> check(String email) {
        log.info("email 컨트롤러에 도착: " + email);
        if (email.trim().equals("")) {
            return ResponseEntity.badRequest().body("이메일이 없습니다!");
        }
        
        boolean resultFlag = userService.isDuplicate(email);
        log.info("이메일 {}가 중복인가? -> {}", email, resultFlag);
        
        return ResponseEntity.ok().body(resultFlag);
    }
    
    
    //회원 가입 요청 처리
    //POST : /api/auth
    @PostMapping
    public ResponseEntity<?> signup(
       @Validated @RequestPart("user") UserRequestSignUpDTO dto,
       @RequestPart(value = "profileImage", required = false) MultipartFile profileImg,
       //이미지가 여러개면 List<MultipartFile>로 선언! // required는 필수의 라는 뜻. 값이 안올 수 있다면 false로 줘야함.
       BindingResult result
    ) {
        
        log.info("/api/auth POST - {}", dto);
        log.info("들어온 이미지 - {}", profileImg);
        
        if (result.hasErrors()) {
            log.warn("유저 컨트롤러 result.toString(): " + result.toString());
            return ResponseEntity.badRequest().body(result.getFieldError());
        }
        
        try {
            String uploadedFilePath = null;
            if (profileImg != null) {
                log.info("attached file name: {}", profileImg.getOriginalFilename());
                uploadedFilePath = userService.uploadProfileImage(profileImg);
            }
            
            UserSignUpResponseDTO responseDTO = userService.create(dto, uploadedFilePath);
            return ResponseEntity.ok().body(responseDTO);
            
        } catch (NoRegisteredArgumentsException e) {
            log.warn("필수 가입정보를 전달받지못했습니다.");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DuplicatedEmailException e) {
            log.warn("이메일이 중복되었습니다.");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            //파일 저장에서 문제 발생
            log.warn("기타 예외가 발생하였습니다.");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
        
    }
    
    
    // 로그인 요청 처리
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody @Validated LoginRequestDTO dto) {
        
        try {
            LoginResponseDTO responseDTO = userService.authenticate(dto);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 일반 회원을 프리미엄 회원으로 승격하는 요청 처리
    @PutMapping("/promote")
    // 권한 검사 (해당 권한이 아니라면 인가처리 거부 403 코드 리턴)
    @PreAuthorize("hasRole('ROLE_COMMON')") //ROLE_COMMON이 아니면 다 내침.
    public ResponseEntity<?> promote(
       @AuthenticationPrincipal TokenUserInfo userInfo
    ) {
        log.info("승급 요청! /api/auth/promote - PUT !");
        
        try {
            LoginResponseDTO responseDTO = userService.promoteToPremium(userInfo);
            return ResponseEntity.ok().body(responseDTO);
        } catch (IllegalStateException | NoRegisteredArgumentsException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 프로필 사진 이미지 데이터를 클라이언트에게 응답 처리
    // -> S3로 대체한 이후 이 메서드는 사용하지 않음.
    @GetMapping("/load-profile")
    public ResponseEntity<?> loadFile(
       @AuthenticationPrincipal TokenUserInfo userInfo
    ){
        log.info("/api/auth/load-profile - GET! user : {}", userInfo.getEmail());
        
        try {
            // 클라이언트가 요청한 프로필 사진을 응답해야 함
            // 1. 프로필 사진의 경로를 얻어야 함
            String filePath = userService.findProfilePath(userInfo.getUserId());
            
            // 2. 얻어낸 파일 경로를 통해 실제 파일 데이터 로드하기.
            File profileFile = new File(filePath); //경로 문제
            
            if(!profileFile.exists()){  //파일이 없으면 404 뚜ㅏ유가,
                return ResponseEntity.notFound().build();
            }
            
            //해당 경로에 저장된 파일을 바이트 계열로 작렬화 해서 리턴
            byte[] fileData = FileCopyUtils.copyToByteArray(profileFile);
            
            //3. 응답 헤더에 컨텐츠 타입을 설정.
            HttpHeaders headers = new HttpHeaders();
            MediaType contentType = findExtensionAndGetMediaType(filePath);
            if(contentType == null){
                return ResponseEntity.internalServerError().body("이미지가 아닌 파일이 발견되었습니다.");
            }
            headers.setContentType(contentType);
            
            return ResponseEntity.ok().headers(headers).body(fileData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("파일을 찾을 수 없습니다.");
        }
    }
    
    
    // 사용자가 올린 파일의 속성을 파악하는 메서드
    private MediaType findExtensionAndGetMediaType(String filePath) {
        
        // 파일 경로에서 확장자 추출하기
        // C:/todo_upload/fjelfjelhfnwuqroxn_img.jpg
        String ext = filePath.substring(filePath.lastIndexOf(".") + 1);
        
        switch (ext.toUpperCase()){
            case "JPG": case "JPEG":
                return MediaType.IMAGE_JPEG;
            case "PNG":
                return MediaType.IMAGE_PNG;
            case "GIF":
                return MediaType.IMAGE_GIF;
            default:
                return null;
        }
    }
    
    
    //프로필 사진을 주는 메서드. (로그인과 함께 주는게 편함!)
    @GetMapping("/load-s3")
    public ResponseEntity<?> loadS3(
       @AuthenticationPrincipal TokenUserInfo userInfo //인증된 토큰 받아오기!
        ) {
        
        log.info("/api/auth/load-3 GET - user: {}", userInfo);
        
        try {
            String profilePath = userService.findProfilePath(userInfo.getUserId()); //링크가 돌아옴!
            return ResponseEntity.ok().body(profilePath);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
    }
    
    
    
    
}
